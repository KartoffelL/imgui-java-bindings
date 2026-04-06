package imgui.app.glfw;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import java.lang.foreign.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Random;

import imguijb.*;
import imgui.app.ImGuiUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * This is a somewhat modified version of a part of SpaiR's ImGui java library (https://github.com/SpaiR/imgui-java)
 *
 * This class is a straightforward port of the
 * <a href="https://raw.githubusercontent.com/ocornut/imgui/1ee252772ae9c0a971d06257bb5c89f628fa696a/backends/imgui_impl_glfw.cpp">imgui_impl_glfw.cpp</a>.
 * <p>
 * It supports clipboard, gamepad, mouse and keyboard in the same way the original Dear ImGui code does. You can copy-paste this class in your codebase and
 * modify the rendering routine in the way you'd like.
 */
@SuppressWarnings({"checkstyle:DesignForExtension", "checkstyle:NeedBraces", "checkstyle:LocalVariableName", "checkstyle:FinalLocalVariable", "checkstyle:ParameterName", "checkstyle:EmptyBlock", "checkstyle:AvoidNestedBlocks"})
public class ImGuiImplGlfw {
    protected static final String OS = System.getProperty("os.name", "generic").toLowerCase();
    protected static final boolean IS_WINDOWS = OS.contains("win");
    protected static final boolean IS_APPLE = OS.contains("mac") || OS.contains("darwin");

    /**
     * Data class to store implementation specific fields.
     * Same as {@code ImGui_ImplGlfw_Data}.
     */
    protected static class Data {
        protected long window = -1;
        protected double time = 0.0;
        protected long mouseWindow = -1;
        protected long[] mouseCursors = new long[ImGuiMouseCursor_.ImGuiMouseCursor_COUNT];
        protected ImVec2 lastValidMousePos = new ImVec2();
        protected long[] keyOwnerWindows = new long[GLFW_KEY_LAST];
        protected boolean installedCallbacks = false;
        protected boolean callbacksChainForAllWindows = false;
        protected boolean wantUpdateMonitors = true;

        // Chain GLFW callbacks: our callbacks will call the user's previously installed callbacks, if any.
        protected GLFWWindowFocusCallback prevUserCallbackWindowFocus = null;
        protected GLFWCursorPosCallback prevUserCallbackCursorPos = null;
        protected GLFWCursorEnterCallback prevUserCallbackCursorEnter = null;
        protected GLFWMouseButtonCallback prevUserCallbackMousebutton = null;
        protected GLFWScrollCallback prevUserCallbackScroll = null;
        protected GLFWKeyCallback prevUserCallbackKey = null;
        protected GLFWCharCallback prevUserCallbackChar = null;
        protected GLFWMonitorCallback prevUserCallbackMonitor = null;

        // This field is required to use GLFW with touch screens on Windows.
        // For compatibility reasons it was added here as a comment. But we don't use somewhere in the binding implementation.
        // protected long prevWndProc;
    }

    /**
     * Internal class to store containers for frequently used arrays.
     * This class helps minimize the number of object allocations on the JVM side,
     * thereby improving performance and reducing garbage collection overhead.
     */
    private static final class Properties {
        private final int[] windowW = new int[1];
        private final int[] windowH = new int[1];
        private final int[] windowX = new int[1];
        private final int[] windowY = new int[1];
        private final int[] displayW = new int[1];
        private final int[] displayH = new int[1];

        // For mouse tracking
        private final ImVec2 mousePosPrev = new ImVec2();
        private final double[] mouseX = new double[1];
        private final double[] mouseY = new double[1];

        // Monitor properties
        private final int[] monitorX = new int[1];
        private final int[] monitorY = new int[1];
        private final int[] monitorWorkAreaX = new int[1];
        private final int[] monitorWorkAreaY = new int[1];
        private final int[] monitorWorkAreaWidth = new int[1];
        private final int[] monitorWorkAreaHeight = new int[1];
        private final float[] monitorContentScaleX = new float[1];
        private final float[] monitorContentScaleY = new float[1];

        // For char translation
        private final String charNames = "`-=[]\\,;'./";
        private final int[] charKeys = {
                GLFW_KEY_GRAVE_ACCENT, GLFW_KEY_MINUS, GLFW_KEY_EQUAL, GLFW_KEY_LEFT_BRACKET,
                GLFW_KEY_RIGHT_BRACKET, GLFW_KEY_BACKSLASH, GLFW_KEY_COMMA, GLFW_KEY_SEMICOLON,
                GLFW_KEY_APOSTROPHE, GLFW_KEY_PERIOD, GLFW_KEY_SLASH
        };
    }

    protected Data data = null;
    private final Properties props = new Properties();

    // We gather version tests as define in order to easily see which features are version-dependent.
    protected static final int glfwVersionCombined = GLFW_VERSION_MAJOR * 1000 + GLFW_VERSION_MINOR * 100 + GLFW_VERSION_REVISION;
    protected static final boolean glfwHawWindowTopmost = glfwVersionCombined >= 3200; // 3.2+ GLFW_FLOATING
    protected static final boolean glfwHasWindowHovered = glfwVersionCombined >= 3300; // 3.3+ GLFW_HOVERED
    protected static final boolean glfwHasWindowAlpha = glfwVersionCombined >= 3300; // 3.3+ glfwSetWindowOpacity
    protected static final boolean glfwHasPerMonitorDpi = glfwVersionCombined >= 3300; // 3.3+ glfwGetMonitorContentScale
    // protected boolean glfwHasVulkan; TODO: I want to believe...
    protected static final boolean glfwHasFocusWindow = glfwVersionCombined >= 3200; // 3.2+ glfwFocusWindow
    protected static final boolean glfwHasFocusOnShow = glfwVersionCombined >= 3300; // 3.3+ GLFW_FOCUS_ON_SHOW
    protected static final boolean glfwHasMonitorWorkArea = glfwVersionCombined >= 3300; // 3.3+ glfwGetMonitorWorkarea
    protected static final boolean glfwHasOsxWindowPosFix = glfwVersionCombined >= 3301; // 3.3.1+ Fixed: Resizing window repositions it on MacOS #1553
    protected static final boolean glfwHasNewCursors = glfwVersionCombined >= 3400; // 3.4+ GLFW_RESIZE_ALL_CURSOR, GLFW_RESIZE_NESW_CURSOR, GLFW_RESIZE_NWSE_CURSOR, GLFW_NOT_ALLOWED_CURSOR
    protected static final boolean glfwHasMousePassthrough = glfwVersionCombined >= 3400; // 3.4+ GLFW_MOUSE_PASSTHROUGH
    protected static final boolean glfwHasGamepadApi = glfwVersionCombined >= 3300; // 3.3+ glfwGetGamepadState() new api
    protected static final boolean glfwHasGetKeyName = glfwVersionCombined >= 3200; // 3.2+ glfwGetKeyName()
    protected static final boolean glfwHasGetError = glfwVersionCombined >= 3300; // 3.3+ glfwGetError()

    protected static final HashMap<Long, Object> USER_DATA = new HashMap<>();
    private static final Random random = new Random();

    protected static <T> T getUserData(long l) {
        return (T) USER_DATA.get(l);
    }
    protected static long putUserData(Object o) {
        long a = random.nextLong();
        USER_DATA.put(a, o);
        return a;
    }


    protected  SWIGTYPE_p_f_p_void__p_char getClipboardTextFn() {
        return new ImGuiUtil.SWIGTYPE_p_f_p_void__p_charImpl((seg)->{
            long str = nglfwGetClipboardString(data.window);
            return MemorySegment.ofAddress(str);
        }, Arena.global());
    }

    protected SWIGTYPE_p_f_p_void_p_q_const__char__void setClipboardTextFn() {
        return new ImGuiUtil.SWIGTYPE_p_f_p_void_p_q_const__char__voidImpl((seg, str)->{
            GLFW.nglfwSetClipboardString(data.window, str.address());
            return MemorySegment.NULL;
        }, Arena.global());
    }

    protected int glfwKeyToImGuiKey(final int glfwKey) {
        switch (glfwKey) {
            case GLFW_KEY_TAB:
                return ImGuiKey.ImGuiKey_Tab;
            case GLFW_KEY_LEFT:
                return ImGuiKey.ImGuiKey_LeftArrow;
            case GLFW_KEY_RIGHT:
                return ImGuiKey.ImGuiKey_RightArrow;
            case GLFW_KEY_UP:
                return ImGuiKey.ImGuiKey_UpArrow;
            case GLFW_KEY_DOWN:
                return ImGuiKey.ImGuiKey_DownArrow;
            case GLFW_KEY_PAGE_UP:
                return ImGuiKey.ImGuiKey_PageUp;
            case GLFW_KEY_PAGE_DOWN:
                return ImGuiKey.ImGuiKey_PageDown;
            case GLFW_KEY_HOME:
                return ImGuiKey.ImGuiKey_Home;
            case GLFW_KEY_END:
                return ImGuiKey.ImGuiKey_End;
            case GLFW_KEY_INSERT:
                return ImGuiKey.ImGuiKey_Insert;
            case GLFW_KEY_DELETE:
                return ImGuiKey.ImGuiKey_Delete;
            case GLFW_KEY_BACKSPACE:
                return ImGuiKey.ImGuiKey_Backspace;
            case GLFW_KEY_SPACE:
                return ImGuiKey.ImGuiKey_Space;
            case GLFW_KEY_ENTER:
                return ImGuiKey.ImGuiKey_Enter;
            case GLFW_KEY_ESCAPE:
                return ImGuiKey.ImGuiKey_Escape;
            case GLFW_KEY_APOSTROPHE:
                return ImGuiKey.ImGuiKey_Apostrophe;
            case GLFW_KEY_COMMA:
                return ImGuiKey.ImGuiKey_Comma;
            case GLFW_KEY_MINUS:
                return ImGuiKey.ImGuiKey_Minus;
            case GLFW_KEY_PERIOD:
                return ImGuiKey.ImGuiKey_Period;
            case GLFW_KEY_SLASH:
                return ImGuiKey.ImGuiKey_Slash;
            case GLFW_KEY_SEMICOLON:
                return ImGuiKey.ImGuiKey_Semicolon;
            case GLFW_KEY_EQUAL:
                return ImGuiKey.ImGuiKey_KeypadEqual;
            case GLFW_KEY_LEFT_BRACKET:
                return ImGuiKey.ImGuiKey_LeftBracket;
            case GLFW_KEY_BACKSLASH:
                return ImGuiKey.ImGuiKey_Backslash;
            case GLFW_KEY_RIGHT_BRACKET:
                return ImGuiKey.ImGuiKey_RightBracket;
            case GLFW_KEY_GRAVE_ACCENT:
                return ImGuiKey.ImGuiKey_GraveAccent;
            case GLFW_KEY_CAPS_LOCK:
                return ImGuiKey.ImGuiKey_CapsLock;
            case GLFW_KEY_SCROLL_LOCK:
                return ImGuiKey.ImGuiKey_ScrollLock;
            case GLFW_KEY_NUM_LOCK:
                return ImGuiKey.ImGuiKey_NumLock;
            case GLFW_KEY_PRINT_SCREEN:
                return ImGuiKey.ImGuiKey_PrintScreen;
            case GLFW_KEY_PAUSE:
                return ImGuiKey.ImGuiKey_Pause;
            case GLFW_KEY_KP_0:
                return ImGuiKey.ImGuiKey_Keypad0;
            case GLFW_KEY_KP_1:
                return ImGuiKey.ImGuiKey_Keypad1;
            case GLFW_KEY_KP_2:
                return ImGuiKey.ImGuiKey_Keypad2;
            case GLFW_KEY_KP_3:
                return ImGuiKey.ImGuiKey_Keypad3;
            case GLFW_KEY_KP_4:
                return ImGuiKey.ImGuiKey_Keypad4;
            case GLFW_KEY_KP_5:
                return ImGuiKey.ImGuiKey_Keypad5;
            case GLFW_KEY_KP_6:
                return ImGuiKey.ImGuiKey_Keypad6;
            case GLFW_KEY_KP_7:
                return ImGuiKey.ImGuiKey_Keypad7;
            case GLFW_KEY_KP_8:
                return ImGuiKey.ImGuiKey_Keypad8;
            case GLFW_KEY_KP_9:
                return ImGuiKey.ImGuiKey_Keypad9;
            case GLFW_KEY_KP_DECIMAL:
                return ImGuiKey.ImGuiKey_KeypadDecimal;
            case GLFW_KEY_KP_DIVIDE:
                return ImGuiKey.ImGuiKey_KeypadDivide;
            case GLFW_KEY_KP_MULTIPLY:
                return ImGuiKey.ImGuiKey_KeypadMultiply;
            case GLFW_KEY_KP_SUBTRACT:
                return ImGuiKey.ImGuiKey_KeypadSubtract;
            case GLFW_KEY_KP_ADD:
                return ImGuiKey.ImGuiKey_KeypadAdd;
            case GLFW_KEY_KP_ENTER:
                return ImGuiKey.ImGuiKey_KeypadEnter;
            case GLFW_KEY_KP_EQUAL:
                return ImGuiKey.ImGuiKey_KeypadEqual;
            case GLFW_KEY_LEFT_SHIFT:
                return ImGuiKey.ImGuiKey_LeftShift;
            case GLFW_KEY_LEFT_CONTROL:
                return ImGuiKey.ImGuiKey_LeftCtrl;
            case GLFW_KEY_LEFT_ALT:
                return ImGuiKey.ImGuiKey_LeftAlt;
            case GLFW_KEY_LEFT_SUPER:
                return ImGuiKey.ImGuiKey_LeftSuper;
            case GLFW_KEY_RIGHT_SHIFT:
                return ImGuiKey.ImGuiKey_RightShift;
            case GLFW_KEY_RIGHT_CONTROL:
                return ImGuiKey.ImGuiKey_RightCtrl;
            case GLFW_KEY_RIGHT_ALT:
                return ImGuiKey.ImGuiKey_RightAlt;
            case GLFW_KEY_RIGHT_SUPER:
                return ImGuiKey.ImGuiKey_RightSuper;
            case GLFW_KEY_MENU:
                return ImGuiKey.ImGuiKey_Menu;
            case GLFW_KEY_0:
                return ImGuiKey.ImGuiKey_0;
            case GLFW_KEY_1:
                return ImGuiKey.ImGuiKey_1;
            case GLFW_KEY_2:
                return ImGuiKey.ImGuiKey_2;
            case GLFW_KEY_3:
                return ImGuiKey.ImGuiKey_3;
            case GLFW_KEY_4:
                return ImGuiKey.ImGuiKey_4;
            case GLFW_KEY_5:
                return ImGuiKey.ImGuiKey_5;
            case GLFW_KEY_6:
                return ImGuiKey.ImGuiKey_6;
            case GLFW_KEY_7:
                return ImGuiKey.ImGuiKey_7;
            case GLFW_KEY_8:
                return ImGuiKey.ImGuiKey_8;
            case GLFW_KEY_9:
                return ImGuiKey.ImGuiKey_9;
            case GLFW_KEY_A:
                return ImGuiKey.ImGuiKey_A;
            case GLFW_KEY_B:
                return ImGuiKey.ImGuiKey_B;
            case GLFW_KEY_C:
                return ImGuiKey.ImGuiKey_C;
            case GLFW_KEY_D:
                return ImGuiKey.ImGuiKey_D;
            case GLFW_KEY_E:
                return ImGuiKey.ImGuiKey_E;
            case GLFW_KEY_F:
                return ImGuiKey.ImGuiKey_F;
            case GLFW_KEY_G:
                return ImGuiKey.ImGuiKey_G;
            case GLFW_KEY_H:
                return ImGuiKey.ImGuiKey_H;
            case GLFW_KEY_I:
                return ImGuiKey.ImGuiKey_I;
            case GLFW_KEY_J:
                return ImGuiKey.ImGuiKey_J;
            case GLFW_KEY_K:
                return ImGuiKey.ImGuiKey_K;
            case GLFW_KEY_L:
                return ImGuiKey.ImGuiKey_L;
            case GLFW_KEY_M:
                return ImGuiKey.ImGuiKey_M;
            case GLFW_KEY_N:
                return ImGuiKey.ImGuiKey_N;
            case GLFW_KEY_O:
                return ImGuiKey.ImGuiKey_O;
            case GLFW_KEY_P:
                return ImGuiKey.ImGuiKey_P;
            case GLFW_KEY_Q:
                return ImGuiKey.ImGuiKey_Q;
            case GLFW_KEY_R:
                return ImGuiKey.ImGuiKey_R;
            case GLFW_KEY_S:
                return ImGuiKey.ImGuiKey_S;
            case GLFW_KEY_T:
                return ImGuiKey.ImGuiKey_T;
            case GLFW_KEY_U:
                return ImGuiKey.ImGuiKey_U;
            case GLFW_KEY_V:
                return ImGuiKey.ImGuiKey_V;
            case GLFW_KEY_W:
                return ImGuiKey.ImGuiKey_W;
            case GLFW_KEY_X:
                return ImGuiKey.ImGuiKey_X;
            case GLFW_KEY_Y:
                return ImGuiKey.ImGuiKey_Y;
            case GLFW_KEY_Z:
                return ImGuiKey.ImGuiKey_Z;
            case GLFW_KEY_F1:
                return ImGuiKey.ImGuiKey_F1;
            case GLFW_KEY_F2:
                return ImGuiKey.ImGuiKey_F2;
            case GLFW_KEY_F3:
                return ImGuiKey.ImGuiKey_F3;
            case GLFW_KEY_F4:
                return ImGuiKey.ImGuiKey_F4;
            case GLFW_KEY_F5:
                return ImGuiKey.ImGuiKey_F5;
            case GLFW_KEY_F6:
                return ImGuiKey.ImGuiKey_F6;
            case GLFW_KEY_F7:
                return ImGuiKey.ImGuiKey_F7;
            case GLFW_KEY_F8:
                return ImGuiKey.ImGuiKey_F8;
            case GLFW_KEY_F9:
                return ImGuiKey.ImGuiKey_F9;
            case GLFW_KEY_F10:
                return ImGuiKey.ImGuiKey_F10;
            case GLFW_KEY_F11:
                return ImGuiKey.ImGuiKey_F11;
            case GLFW_KEY_F12:
                return ImGuiKey.ImGuiKey_F12;
            case GLFW_KEY_F13:
                return ImGuiKey.ImGuiKey_F13;
            case GLFW_KEY_F14:
                return ImGuiKey.ImGuiKey_F14;
            case GLFW_KEY_F15:
                return ImGuiKey.ImGuiKey_F15;
            case GLFW_KEY_F16:
                return ImGuiKey.ImGuiKey_F16;
            case GLFW_KEY_F17:
                return ImGuiKey.ImGuiKey_F17;
            case GLFW_KEY_F18:
                return ImGuiKey.ImGuiKey_F18;
            case GLFW_KEY_F19:
                return ImGuiKey.ImGuiKey_F19;
            case GLFW_KEY_F20:
                return ImGuiKey.ImGuiKey_F20;
            case GLFW_KEY_F21:
                return ImGuiKey.ImGuiKey_F21;
            case GLFW_KEY_F22:
                return ImGuiKey.ImGuiKey_F22;
            case GLFW_KEY_F23:
                return ImGuiKey.ImGuiKey_F23;
            case GLFW_KEY_F24:
                return ImGuiKey.ImGuiKey_F24;
            default:
                return ImGuiKey.ImGuiKey_None;
        }
    }

    // X11 does not include current pressed/released modifier key in 'mods' flags submitted by GLFW
    // See https://github.com/ocornut/imgui/issues/6034 and https://github.com/glfw/glfw/issues/1630
    protected void updateKeyModifiers(final long window) {
        final ImGuiIO io = ImGui.GetIO();
        io.AddKeyEvent(ImGuiKey.ImGuiMod_Ctrl, (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) || (glfwGetKey(window, GLFW_KEY_RIGHT_CONTROL) == GLFW_PRESS));
        io.AddKeyEvent(ImGuiKey.ImGuiMod_Shift, (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) || (glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) == GLFW_PRESS));
        io.AddKeyEvent(ImGuiKey.ImGuiMod_Alt, (glfwGetKey(window, GLFW_KEY_LEFT_ALT) == GLFW_PRESS) || (glfwGetKey(window, GLFW_KEY_RIGHT_ALT) == GLFW_PRESS));
        io.AddKeyEvent(ImGuiKey.ImGuiMod_Super, (glfwGetKey(window, GLFW_KEY_LEFT_SUPER) == GLFW_PRESS) || (glfwGetKey(window, GLFW_KEY_RIGHT_SUPER) == GLFW_PRESS));
    }

    protected boolean shouldChainCallback(final long window) {
        return data.callbacksChainForAllWindows ? true : (window == data.window);
    }

    public void mouseButtonCallback(final long window, final int button, final int action, final int mods) {
        if (data.prevUserCallbackMousebutton != null && shouldChainCallback(window)) {
            data.prevUserCallbackMousebutton.invoke(window, button, action, mods);
        }

        updateKeyModifiers(window);

        final ImGuiIO io = ImGui.GetIO();
        if (button >= 0 && button < ImGuiMouseButton_.ImGuiMouseButton_COUNT) {
            io.AddMouseButtonEvent(button, action == GLFW_PRESS);
        }
    }

    public void scrollCallback(final long window, final double xOffset, final double yOffset) {
        if (data.prevUserCallbackScroll != null && shouldChainCallback(window)) {
            data.prevUserCallbackScroll.invoke(window, xOffset, yOffset);
        }

        final ImGuiIO io = ImGui.GetIO();
        io.AddMouseWheelEvent((float) xOffset, (float) yOffset);
    }

    protected int translateUntranslatedKey(final int key, final int scancode) {
        if (!glfwHasGetKeyName) {
            return key;
        }

        // GLFW 3.1+ attempts to "untranslate" keys, which goes the opposite of what every other framework does, making using lettered shortcuts difficult.
        // (It had reasons to do so: namely GLFW is/was more likely to be used for WASD-type game controls rather than lettered shortcuts, but IHMO the 3.1 change could have been done differently)
        // See https://github.com/glfw/glfw/issues/1502 for details.
        // Adding a workaround to undo this (so our keys are translated->untranslated->translated, likely a lossy process).
        // This won't cover edge cases but this is at least going to cover common cases.
        if (key >= GLFW_KEY_KP_0 && key <= GLFW_KEY_KP_EQUAL) {
            return key;
        }

        int resultKey = key;
        final String keyName = glfwGetKeyName(key, scancode);
        eatErrors();
        if (keyName != null && keyName.length() > 2 && keyName.charAt(0) != 0 && keyName.charAt(1) == 0) {
            if (keyName.charAt(0) >= '0' && keyName.charAt(0) <= '9') {
                resultKey = GLFW_KEY_0 + (keyName.charAt(0) - '0');
            } else if (keyName.charAt(0) >= 'A' && keyName.charAt(0) <= 'Z') {
                resultKey = GLFW_KEY_A + (keyName.charAt(0) - 'A');
            } else if (keyName.charAt(0) >= 'a' && keyName.charAt(0) <= 'z') {
                resultKey = GLFW_KEY_A + (keyName.charAt(0) - 'a');
            } else {
                final int index = props.charNames.indexOf(keyName.charAt(0));
                if (index != -1) {
                    resultKey = props.charKeys[index];
                }
            }
        }

        return resultKey;
    }

    protected void eatErrors() {
        if (glfwHasGetError) { // Eat errors (see #5908)
            final PointerBuffer pb = MemoryUtil.memAllocPointer(1);
            glfwGetError(pb);
            MemoryUtil.memFree(pb);
        }
    }

    public void keyCallback(final long window, final int keycode, final int scancode, final int action, final int mods) {
        if (data.prevUserCallbackKey != null && shouldChainCallback(window)) {
            data.prevUserCallbackKey.invoke(window, keycode, scancode, action, mods);
        }

        if (action != GLFW_PRESS && action != GLFW_RELEASE) {
            return;
        }

        updateKeyModifiers(window);

        if (keycode >= 0 && keycode < data.keyOwnerWindows.length) {
            data.keyOwnerWindows[keycode] = (action == GLFW_PRESS) ? window : -1;
        }

        final int key = translateUntranslatedKey(keycode, scancode);

        final ImGuiIO io = ImGui.GetIO();
        final var imguiKey = glfwKeyToImGuiKey(key);
        io.AddKeyEvent(imguiKey, (action == GLFW_PRESS));
        io.SetKeyEventNativeData(imguiKey, key, scancode); // To support legacy indexing (<1.87 user code)
    }

    public void windowFocusCallback(final long window, final boolean focused) {
        if (data.prevUserCallbackWindowFocus != null && shouldChainCallback(window)) {
            data.prevUserCallbackWindowFocus.invoke(window, focused);
        }

        ImGui.GetIO().AddFocusEvent(focused);
    }

    public void cursorPosCallback(final long window, final double x, final double y) {
        if (data.prevUserCallbackCursorPos != null && shouldChainCallback(window)) {
            data.prevUserCallbackCursorPos.invoke(window, x, y);
        }

        float posX = (float) x;
        float posY = (float) y;

        final ImGuiIO io = ImGui.GetIO();
        if ((io.getConfigFlags()&ImGuiConfigFlags_.ImGuiConfigFlags_ViewportsEnable)!=0) { //Flag does not exist anymore
            glfwGetWindowPos(window, props.windowX, props.windowY);
            posX += props.windowX[0];
            posY += props.windowY[0];
        }

        io.AddMousePosEvent(posX, posY);
        data.lastValidMousePos.setX(posX);
        data.lastValidMousePos.setY(posY);
    }

    // Workaround: X11 seems to send spurious Leave/Enter events which would make us lose our position,
    // so we back it up and restore on Leave/Enter (see https://github.com/ocornut/imgui/issues/4984)
    public void cursorEnterCallback(final long window, final boolean entered) {
        if (data.prevUserCallbackCursorEnter != null && shouldChainCallback(window)) {
            data.prevUserCallbackCursorEnter.invoke(window, entered);
        }

        final ImGuiIO io = ImGui.GetIO();

        if (entered) {
            data.mouseWindow = window;
            io.AddMousePosEvent(data.lastValidMousePos.getX(), data.lastValidMousePos.getY());
        } else if (data.mouseWindow == window) {
            var mp = io.getMousePos();
            data.lastValidMousePos.setX(mp.getX());
            data.lastValidMousePos.setY(mp.getY());
            data.mouseWindow = -1;
            io.AddMousePosEvent(Float.MIN_VALUE, Float.MIN_VALUE);
        }
    }

    public void charCallback(final long window, final int c) {
        if (data.prevUserCallbackChar != null && shouldChainCallback(window)) {
            data.prevUserCallbackChar.invoke(window, c);
        }

        ImGui.GetIO().AddInputCharacter(c);
    }

    public void monitorCallback(final long window, final int event) {
        data.wantUpdateMonitors = true;
    }

    public void installCallbacks(final long window) {
        data.prevUserCallbackWindowFocus = glfwSetWindowFocusCallback(window, this::windowFocusCallback);
        data.prevUserCallbackCursorEnter = glfwSetCursorEnterCallback(window, this::cursorEnterCallback);
        data.prevUserCallbackCursorPos = glfwSetCursorPosCallback(window, this::cursorPosCallback);
        data.prevUserCallbackMousebutton = glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        data.prevUserCallbackScroll = glfwSetScrollCallback(window, this::scrollCallback);
        data.prevUserCallbackKey = glfwSetKeyCallback(window, this::keyCallback);
        data.prevUserCallbackChar = glfwSetCharCallback(window, this::charCallback);
        data.prevUserCallbackMonitor = glfwSetMonitorCallback(this::monitorCallback);
        data.installedCallbacks = true;
    }

    protected void freeCallback(final Callback cb) {
        if (cb != null) {
            cb.free();
        }
    }

    public void restoreCallbacks(final long window) {
        freeCallback(glfwSetWindowFocusCallback(window, data.prevUserCallbackWindowFocus));
        freeCallback(glfwSetCursorEnterCallback(window, data.prevUserCallbackCursorEnter));
        freeCallback(glfwSetCursorPosCallback(window, data.prevUserCallbackCursorPos));
        freeCallback(glfwSetMouseButtonCallback(window, data.prevUserCallbackMousebutton));
        freeCallback(glfwSetScrollCallback(window, data.prevUserCallbackScroll));
        freeCallback(glfwSetKeyCallback(window, data.prevUserCallbackKey));
        freeCallback(glfwSetCharCallback(window, data.prevUserCallbackChar));
        freeCallback(glfwSetMonitorCallback(data.prevUserCallbackMonitor));
        data.installedCallbacks = false;
        data.prevUserCallbackWindowFocus = null;
        data.prevUserCallbackCursorEnter = null;
        data.prevUserCallbackCursorPos = null;
        data.prevUserCallbackMousebutton = null;
        data.prevUserCallbackScroll = null;
        data.prevUserCallbackKey = null;
        data.prevUserCallbackChar = null;
        data.prevUserCallbackMonitor = null;
    }

    /**
     * Set to 'true' to enable chaining installed callbacks for all windows (including secondary viewports created by backends or by user.
     * This is 'false' by default meaning we only chain callbacks for the main viewport.
     * We cannot set this to 'true' by default because user callbacks code may be not testing the 'window' parameter of their callback.
     * If you set this to 'true' your user callback code will need to make sure you are testing the 'window' parameter.
     */
    public void setCallbacksChainForAllWindows(final boolean chainForAllWindows) {
        data.callbacksChainForAllWindows = chainForAllWindows;
    }

    protected Data newData() {
        return new Data();
    }

    public boolean init(final long window, final boolean installCallbacks) {
        final ImGuiIO io = ImGui.GetIO();

        io.setBackendPlatformName("imgui-java_impl_glfw");
        io.setBackendFlags(io.getBackendFlags()|ImGuiBackendFlags_.ImGuiBackendFlags_HasMouseCursors | ImGuiBackendFlags_.ImGuiBackendFlags_HasSetMousePos/* | ImGuiBackendFlags_.PlatformHasViewports*/); //Viewport flags are not present anymore
        if (glfwHasMousePassthrough || (glfwHasWindowHovered && IS_WINDOWS)) {
            io.setBackendFlags(io.getBackendFlags()|ImGuiBackendFlags_.ImGuiBackendFlags_HasMouseHoveredViewport);
        }

        data = newData();
        data.window = window;
        data.time = 0.0;
        data.wantUpdateMonitors = true;


        io.setGetClipboardTextFn(getClipboardTextFn());
        io.setSetClipboardTextFn(setClipboardTextFn());

        // Create mouse cursors
        // (By design, on X11 cursors are user configurable and some cursors may be missing. When a cursor doesn't exist,
        // GLFW will emit an error which will often be printed by the app, so we temporarily disable error reporting.
        // Missing cursors will return NULL and our _UpdateMouseCursor() function will use the Arrow cursor instead.)
        final GLFWErrorCallback prevErrorCallback = glfwSetErrorCallback(null);
        data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeNS] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeEW] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        if (glfwHasNewCursors) {
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeAll] = glfwCreateStandardCursor(GLFW_RESIZE_ALL_CURSOR);
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeNESW] = glfwCreateStandardCursor(GLFW_RESIZE_NESW_CURSOR);
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeNWSE] = glfwCreateStandardCursor(GLFW_RESIZE_NWSE_CURSOR);
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_NotAllowed] = glfwCreateStandardCursor(GLFW_NOT_ALLOWED_CURSOR);
        } else {
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeAll] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeNESW] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_ResizeNWSE] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
            data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        }
        glfwSetErrorCallback(prevErrorCallback);
        eatErrors();

        // Chain GLFW callbacks: our callbacks will call the user's previously installed callbacks, if any.
        if (installCallbacks) {
            installCallbacks(window);
        }

        // Update monitors the first time (note: monitor callback are broken in GLFW 3.2 and earlier, see github.com/glfw/glfw/issues/784)
        updateMonitors();
        glfwSetMonitorCallback(this::monitorCallback);

        // Our mouse update function expect PlatformHandle to be filled for the main viewport
        final ImGuiViewport mainViewport = ImGui.GetMainViewport();
        mainViewport.setPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(window));
        if (IS_WINDOWS) {
            mainViewport.setPlatformHandleRaw(ImGuiUtil.newSWIGTYPE_p_void(GLFWNativeWin32.glfwGetWin32Window(window)));
        }
        if (IS_APPLE) {
            mainViewport.setPlatformHandleRaw(ImGuiUtil.newSWIGTYPE_p_void(GLFWNativeCocoa.glfwGetCocoaWindow(window)));
        }
        if ((io.getConfigFlags()&ImGuiConfigFlags_.ImGuiConfigFlags_ViewportsEnable)!=0) {
            initPlatformInterface();
        }

        return true;
    }

    public void shutdown() {
        final ImGuiIO io = ImGui.GetIO();

        shutdownPlatformInterface();

        if (data.installedCallbacks) {
            restoreCallbacks(data.window);
        }

        for (int cursorN = 0; cursorN < ImGuiMouseCursor_.ImGuiMouseCursor_COUNT; cursorN++) {
            if(data.mouseCursors[cursorN] != 0) glfwDestroyCursor(data.mouseCursors[cursorN]);
        }

        io.setBackendPlatformName(null);
        data = null;
        io.setBackendFlags(io.getBackendFlags()&~(ImGuiBackendFlags_.ImGuiBackendFlags_HasMouseCursors | ImGuiBackendFlags_.ImGuiBackendFlags_HasSetMousePos | ImGuiBackendFlags_.ImGuiBackendFlags_HasGamepad
                | ImGuiBackendFlags_.ImGuiBackendFlags_PlatformHasViewports | ImGuiBackendFlags_.ImGuiBackendFlags_HasMouseHoveredViewport));
//        io.removeBackendFlags(ImGuiBackendFlags.HasMouseCursors | ImGuiBackendFlags.HasSetMousePos | ImGuiBackendFlags.HasGamepad
//            | ImGuiBackendFlags.PlatformHasViewports | ImGuiBackendFlags.HasMouseHoveredViewport);
    }

    protected void updateMouseData() {
        final ImGuiIO io = ImGui.GetIO();
        final ImGuiPlatformIO platformIO = ImGui.GetPlatformIO();

        int mouseViewportId = 0;
        var mp = io.getMousePos();
        props.mousePosPrev.setX(mp.getX());
        props.mousePosPrev.setY(mp.getY());
        var viewports = new ImGuiUtil.ImGuiViewportVector(platformIO.getViewports());
        for (int n = 0; n < viewports.size(); n++) {
            final ImGuiViewport viewport = viewports.get(n);
            final long window = ImGuiUtil.getSWIGTYPE_p_void(viewport.getPlatformHandle());
            final boolean isWindowFocused = glfwGetWindowAttrib(window, GLFW_FOCUSED) != 0;

            if (isWindowFocused) {
                // (Optional) Set OS mouse position from Dear ImGui if requested (rarely used, only when ImGuiConfigFlags_NavEnableSetMousePos is enabled by user)
                // When multi-viewports are enabled, all Dear ImGui positions are same as OS positions.
                if (io.getWantSetMousePos()) {
                    var pos = viewport.getPos();
                    glfwSetCursorPos(window, props.mousePosPrev.getY() - pos.getX(), props.mousePosPrev.getY() - pos.getX());
                }

                // (Optional) Fallback to provide mouse position when focused (ImGui_ImplGlfw_CursorPosCallback already provides this when hovered or captured)
                if (data.mouseWindow == -1) {
                    glfwGetCursorPos(window, props.mouseX, props.mouseY);
                    double mouseX = props.mouseX[0];
                    double mouseY = props.mouseY[0];
                    if ((io.getConfigFlags()&ImGuiConfigFlags_.ImGuiConfigFlags_ViewportsEnable) != 0) {
                        // Single viewport mode: mouse position in client window coordinates (io.MousePos is (0,0) when the mouse is on the upper-left corner of the app window)
                        // Multi-viewport mode: mouse position in OS absolute coordinates (io.MousePos is (0,0) when the mouse is on the upper-left of the primary monitor)
                        glfwGetWindowPos(window, props.windowX, props.windowY);
                        mouseX += props.windowX[0];
                        mouseY += props.windowY[0];
                    }
                    data.lastValidMousePos.setX((float) mouseX);
                    data.lastValidMousePos.setY((float) mouseY);
                    io.AddMousePosEvent((float) mouseX, (float) mouseY);
                }
            }

            // (Optional) When using multiple viewports: call io.AddMouseViewportEvent() with the viewport the OS mouse cursor is hovering.
            // If ImGuiBackendFlags_HasMouseHoveredViewport is not set by the backend, Dear imGui will ignore this field and infer the information using its flawed heuristic.
            // - [X] GLFW >= 3.3 backend ON WINDOWS ONLY does correctly ignore viewports with the _NoInputs flag.
            // - [!] GLFW <= 3.2 backend CANNOT correctly ignore viewports with the _NoInputs flag, and CANNOT reported Hovered Viewport because of mouse capture.
            //       Some backend are not able to handle that correctly. If a backend report an hovered viewport that has the _NoInputs flag (e.g. when dragging a window
            //       for docking, the viewport has the _NoInputs flag in order to allow us to find the viewport under), then Dear ImGui is forced to ignore the value reported
            //       by the backend, and use its flawed heuristic to guess the viewport behind.
            // - [X] GLFW backend correctly reports this regardless of another viewport behind focused and dragged from (we need this to find a useful drag and drop target).
            // FIXME: This is currently only correct on Win32. See what we do below with the WM_NCHITTEST, missing an equivalent for other systems.
            // See https://github.com/glfw/glfw/issues/1236 if you want to help in making this a GLFW feature.

            if (glfwHasMousePassthrough || (glfwHasWindowHovered && IS_WINDOWS)) {
                boolean windowNoInput = (viewport.getFlags()&ImGuiViewportFlags_.ImGuiViewportFlags_NoInputs )!=0;
                if (glfwHasMousePassthrough) {
                    glfwSetWindowAttrib(window, GLFW_MOUSE_PASSTHROUGH, windowNoInput ? GLFW_TRUE : GLFW_FALSE);
                }
                if (glfwGetWindowAttrib(window, GLFW_HOVERED) == GLFW_TRUE && !windowNoInput) {
                    mouseViewportId = Math.toIntExact(viewport.getID());
                }
            }
            // else
            // We cannot use bd->MouseWindow maintained from CursorEnter/Leave callbacks, because it is locked to the window capturing mouse.
        }

        if ((io.getBackendFlags()&ImGuiBackendFlags_.ImGuiBackendFlags_HasMouseHoveredViewport) !=0) {
            io.AddMouseViewportEvent(mouseViewportId);
        }
    }

    protected void updateMouseCursor() {
        final ImGuiIO io = ImGui.GetIO();

        if ((io.getBackendFlags()&ImGuiConfigFlags_.ImGuiConfigFlags_NoMouseCursorChange) !=0 || glfwGetInputMode(data.window, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) {
            return;
        }

        final int imguiCursor = ImGui.GetMouseCursor();
        final ImGuiPlatformIO platformIO = ImGui.GetPlatformIO();
        final ImGuiUtil.ImGuiViewportVector viewports = new ImGuiUtil.ImGuiViewportVector(platformIO.getViewports());
        for (int n = 0; n < viewports.size(); n++) {
            final long windowPtr = ImGuiUtil.getSWIGTYPE_p_void(viewports.get(n).getPlatformHandle());

            if (imguiCursor == ImGuiMouseCursor_.ImGuiMouseCursor_None || io.getMouseDrawCursor()) {
                // Hide OS mouse cursor if imgui is drawing it or if it wants no cursor
                glfwSetInputMode(windowPtr, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            } else {
                // Show OS mouse cursor
                // FIXME-PLATFORM: Unfocused windows seems to fail changing the mouse cursor with GLFW 3.2, but 3.3 works here.
                glfwSetCursor(windowPtr, data.mouseCursors[imguiCursor] != 0 ? data.mouseCursors[imguiCursor] : data.mouseCursors[ImGuiMouseCursor_.ImGuiMouseCursor_Arrow]);
                glfwSetInputMode(windowPtr, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }
    }

    @FunctionalInterface
    private interface MapButton {
        void run(int keyNo, int buttonNo, int _unused);
    }

    @FunctionalInterface
    private interface MapAnalog {
        void run(int keyNo, int axisNo, int _unused, float v0, float v1);
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    private float saturate(final float v) {
        return v < 0.0f ? 0.0f : v > 1.0f ? 1.0f : v;
    }

    protected void updateGamepads() {
        final ImGuiIO io = ImGui.GetIO();

        if ((io.getConfigFlags() & ImGuiConfigFlags_.ImGuiConfigFlags_NavEnableGamepad) == 0) {
            return;
        }

        io.setBackendFlags(io.getBackendFlags()&~ImGuiBackendFlags_.ImGuiBackendFlags_HasGamepad); //Remove backend flag

        final MapButton mapButton;
        final MapAnalog mapAnalog;

        if (glfwHasGamepadApi) {
            try (GLFWGamepadState gamepad = GLFWGamepadState.create()) {
                if (!glfwGetGamepadState(GLFW_JOYSTICK_1, gamepad)) {
                    return;
                }
                mapButton = (keyNo, buttonNo, _unused) -> io.AddKeyEvent(keyNo, gamepad.buttons(buttonNo) != 0);
                mapAnalog = (keyNo, axisNo, _unused, v0, v1) -> {
                    float v = gamepad.axes(axisNo);
                    v = (v - v0) / (v1 - v0);
                    io.AddKeyAnalogEvent(keyNo, v > 0.10f, saturate(v));
                };
            }
        } else {
            final FloatBuffer axes = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
            final ByteBuffer buttons = glfwGetJoystickButtons(GLFW_JOYSTICK_1);
            if (axes == null || axes.limit() == 0 || buttons == null || buttons.limit() == 0) {
                return;
            }
            mapButton = (keyNo, buttonNo, _unused) -> io.AddKeyEvent(keyNo, (buttons.limit() > buttonNo && buttons.get(buttonNo) == GLFW_PRESS));
            mapAnalog = (keyNo, axisNo, _unused, v0, v1) -> {
                float v = (axes.limit() > axisNo) ? axes.get(axisNo) : v0;
                v = (v - v0) / (v1 - v0);
                io.AddKeyAnalogEvent(keyNo, v > 0.10f, saturate(v));
            };
        }

        io.setBackendFlags(io.getBackendFlags()|ImGuiBackendFlags_.ImGuiBackendFlags_HasGamepad);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadStart, GLFW_GAMEPAD_BUTTON_START, 7);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadBack, GLFW_GAMEPAD_BUTTON_BACK, 6);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadFaceLeft, GLFW_GAMEPAD_BUTTON_X, 2);     // Xbox X, PS Square
        mapButton.run(ImGuiKey.ImGuiKey_GamepadFaceRight, GLFW_GAMEPAD_BUTTON_B, 1);     // Xbox B, PS Circle
        mapButton.run(ImGuiKey.ImGuiKey_GamepadFaceUp, GLFW_GAMEPAD_BUTTON_Y, 3);     // Xbox Y, PS Triangle
        mapButton.run(ImGuiKey.ImGuiKey_GamepadFaceDown, GLFW_GAMEPAD_BUTTON_A, 0);     // Xbox A, PS Cross
        mapButton.run(ImGuiKey.ImGuiKey_GamepadDpadLeft, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, 13);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadDpadRight, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, 11);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadDpadUp, GLFW_GAMEPAD_BUTTON_DPAD_UP, 10);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadDpadDown, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, 12);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadL1, GLFW_GAMEPAD_BUTTON_LEFT_BUMPER, 4);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadR1, GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER, 5);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadL2, GLFW_GAMEPAD_AXIS_LEFT_TRIGGER, 4, -0.75f, +1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadR2, GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER, 5, -0.75f, +1.0f);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadL3, GLFW_GAMEPAD_BUTTON_LEFT_THUMB, 8);
        mapButton.run(ImGuiKey.ImGuiKey_GamepadR3, GLFW_GAMEPAD_BUTTON_RIGHT_THUMB, 9);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadLStickLeft, GLFW_GAMEPAD_AXIS_LEFT_X, 0, -0.25f, -1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadLStickRight, GLFW_GAMEPAD_AXIS_LEFT_X, 0, +0.25f, +1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadLStickUp, GLFW_GAMEPAD_AXIS_LEFT_Y, 1, -0.25f, -1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadLStickDown, GLFW_GAMEPAD_AXIS_LEFT_Y, 1, +0.25f, +1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadRStickLeft, GLFW_GAMEPAD_AXIS_RIGHT_X, 2, -0.25f, -1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadRStickRight, GLFW_GAMEPAD_AXIS_RIGHT_X, 2, +0.25f, +1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadRStickUp, GLFW_GAMEPAD_AXIS_RIGHT_Y, 3, -0.25f, -1.0f);
        mapAnalog.run(ImGuiKey.ImGuiKey_GamepadRStickDown, GLFW_GAMEPAD_AXIS_RIGHT_Y, 3, +0.25f, +1.0f);
    }

    protected void updateMonitors() {
        final ImGuiPlatformIO platformIO = ImGui.GetPlatformIO();
        data.wantUpdateMonitors = false;

        final PointerBuffer monitors = glfwGetMonitors();
        if (monitors == null) {
            System.err.println("Unable to get monitors!");
            return;
        }
        if (monitors.limit() == 0) { // Preserve existing monitor list if there are none. Happens on macOS sleeping (#5683)
            return;
        }

        var montis = new ImGuiUtil.ImGuiPlatformMonitorVector(platformIO.getMonitors());
        montis.resize(0);

        for (int n = 0; n < monitors.limit(); n++) {
            final long monitor = monitors.get(n);

            final GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            if (vidMode == null) {
                continue;
            }

            glfwGetMonitorPos(monitor, props.monitorX, props.monitorY);

            final float mainPosX = props.monitorX[0];
            final float mainPosY = props.monitorY[0];
            final float mainSizeX = vidMode.width();
            final float mainSizeY = vidMode.height();

            float workPosX = 0;
            float workPosY = 0;
            float workSizeX = 0;
            float workSizeY = 0;

            // Workaround a small GLFW issue reporting zero on monitor changes: https://github.com/glfw/glfw/pull/1761
            if (glfwHasMonitorWorkArea) {
                glfwGetMonitorWorkarea(monitor, props.monitorWorkAreaX, props.monitorWorkAreaY, props.monitorWorkAreaWidth, props.monitorWorkAreaHeight);
                if (props.monitorWorkAreaWidth[0] > 0 && props.monitorWorkAreaHeight[0] > 0) {
                    workPosX = props.monitorWorkAreaX[0];
                    workPosY = props.monitorWorkAreaY[0];
                    workSizeX = props.monitorWorkAreaWidth[0];
                    workSizeY = props.monitorWorkAreaHeight[0];
                }
            }

            float dpiScale = 0;

            // Warning: the validity of monitor DPI information on Windows depends on the application DPI awareness settings,
            // which generally needs to be set in the manifest or at runtime.
            if (glfwHasPerMonitorDpi) {
                glfwGetMonitorContentScale(monitor, props.monitorContentScaleX, props.monitorContentScaleY);
                dpiScale = props.monitorContentScaleX[0];
            }
            var m = new ImGuiPlatformMonitor();
            m.setPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(monitor));
            var mp = m.getMainPos(); //Should work since it's inlined
            mp.setX(mainPosX);
            mp.setY(mainPosY);
            var ms = m.getMainSize(); //Should work since it's inlined
            ms.setX(mainSizeX);
            ms.setY(mainSizeY);
            var wp = m.getWorkPos(); //Should work since it's inlined
            wp.setX(workPosX);
            wp.setY(workPosY);
            var ws = m.getWorkSize(); //Should work since it's inlined
            ws.setX(workSizeX);
            ws.setY(workSizeY);
            m.setDpiScale(dpiScale);
            montis.push(m);
//            platformIO.pushMonitors(monitor, mainPosX, mainPosY, mainSizeX, mainSizeY, workPosX, workPosY, workSizeX, workSizeY, dpiScale);
        }
    }

    public void newFrame() {
        final ImGuiIO io = ImGui.GetIO();

        // Setup display size (every frame to accommodate for window resizing)
        glfwGetWindowSize(data.window, props.windowW, props.windowH);
        glfwGetFramebufferSize(data.window, props.displayW, props.displayH);
        var ds = io.getDisplaySize();
        ds.setX((float) props.windowW[0]);
        ds.setY((float) props.windowH[0]);
//        io.setDisplaySize((float) props.windowW[0], (float) props.windowH[0]);
        if (props.windowW[0] > 0 && props.windowH[0] > 0) {
            final float scaleX = (float) props.displayW[0] / props.windowW[0];
            final float scaleY = (float) props.displayH[0] / props.windowH[0];
            var fs = io.getDisplayFramebufferScale();
            fs.setX(scaleX);
            fs.setY(scaleY);
//            io.setDisplayFramebufferScale(scaleX, scaleY);
        }

        if (data.wantUpdateMonitors) {
            updateMonitors();
        }

        // Setup time step
        // (Accept glfwGetTime() not returning a monotonically increasing value. Seems to happens on disconnecting peripherals and probably on VMs and Emscripten, see #6491, #6189, #6114, #3644)
        double currentTime = glfwGetTime();
        if (currentTime <= data.time) {
            currentTime = data.time + 0.00001f;
        }
        io.setDeltaTime(data.time > 0.0 ? (float) (currentTime - data.time) : 1.0f / 60.0f);
        data.time = currentTime;

        updateMouseData();
        updateMouseCursor();

        // Update game controllers (if enabled and available)
        updateGamepads();
    }

    //--------------------------------------------------------------------------------------------------------
    // MULTI-VIEWPORT / PLATFORM INTERFACE SUPPORT
    // This is an _advanced_ and _optional_ feature, allowing the backend to create and handle multiple viewports simultaneously.
    // If you are new to dear imgui or creating a new binding for dear imgui, it is recommended that you completely ignore this section first..
    //--------------------------------------------------------------------------------------------------------

    private static final class ViewportData {
        long window = -1;
        boolean windowOwned = false;
        int ignoreWindowPosEventFrame = -1;
        int ignoreWindowSizeEventFrame = -1;
    }

    private void windowCloseCallback(final long windowId) {
        final ImGuiViewport vp = ImGui.FindViewportByPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(windowId));
        if (vp != null) {
            vp.setPlatformRequestClose(true);
        }
    }

    // GLFW may dispatch window pos/size events after calling glfwSetWindowPos()/glfwSetWindowSize().
    // However: depending on the platform the callback may be invoked at different time:
    // - on Windows it appears to be called within the glfwSetWindowPos()/glfwSetWindowSize() call
    // - on Linux it is queued and invoked during glfwPollEvents()
    // Because the event doesn't always fire on glfwSetWindowXXX() we use a frame counter tag to only
    // ignore recent glfwSetWindowXXX() calls.
    private void windowPosCallback(final long windowId, final int xPos, final int yPos) {
        final ImGuiViewport vp = ImGui.FindViewportByPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(windowId));
        if (vp == null) {
            return;
        }

        final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
        if (vd != null) {
            final boolean ignoreEvent = (ImGui.GetFrameCount() <= vd.ignoreWindowPosEventFrame + 1);
            if (ignoreEvent) {
                return;
            }
        }

        vp.setPlatformRequestMove(true);
    }

    private void windowSizeCallback(final long windowId, final int width, final int height) {
        final ImGuiViewport vp = ImGui.FindViewportByPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(windowId));
        if (vp == null) {
            return;
        }

        final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
        if (vd != null) {
            final boolean ignoreEvent = (ImGui.GetFrameCount() <= vd.ignoreWindowSizeEventFrame + 1);
            if (ignoreEvent) {
                return;
            }
        }

        vp.setPlatformRequestResize(true);
    }

    private final class CreateWindowFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl.call/* extends ImPlatformFuncViewport*/ {

        @Override
        public void call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = new ViewportData();
            final long vd_ = putUserData(vd);

            // GLFW 3.2 unfortunately always set focus on glfwCreateWindow() if GLFW_VISIBLE is set, regardless of GLFW_FOCUSED
            // With GLFW 3.3, the hint GLFW_FOCUS_ON_SHOW fixes this problem
            glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
            glfwWindowHint(GLFW_FOCUSED, GLFW_FALSE);
            if (glfwHasFocusOnShow) {
                glfwWindowHint(GLFW_FOCUS_ON_SHOW, GLFW_FALSE);
            }
            glfwWindowHint(GLFW_DECORATED, (vp.getFlags()&ImGuiViewportFlags_.ImGuiViewportFlags_NoDecoration)!=0 ? GLFW_FALSE : GLFW_TRUE);
            if (glfwHawWindowTopmost) {
                glfwWindowHint(GLFW_FLOATING, (vp.getFlags()&ImGuiViewportFlags_.ImGuiViewportFlags_TopMost)!=0 ? GLFW_TRUE : GLFW_FALSE);
            }

            var size = vp.getSize();
            vd.window = glfwCreateWindow((int) size.getX(), (int) size.getY(), "No Title Yet", NULL, data.window);
            vd.windowOwned = true;

            vp.setPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(vd.window));

            if (IS_WINDOWS) {
                vp.setPlatformHandleRaw(ImGuiUtil.newSWIGTYPE_p_void(GLFWNativeWin32.glfwGetWin32Window(vd.window)));
            } else if (IS_APPLE) {
                vp.setPlatformHandleRaw(ImGuiUtil.newSWIGTYPE_p_void(GLFWNativeCocoa.glfwGetCocoaWindow(vd.window)));
            }

            var ps = vp.getPos();
            glfwSetWindowPos(vd.window, (int) ps.getX(), (int) ps.getY());

            // Install GLFW callbacks for secondary viewports
            glfwSetWindowFocusCallback(vd.window, ImGuiImplGlfw.this::windowFocusCallback);
            glfwSetCursorEnterCallback(vd.window, ImGuiImplGlfw.this::cursorEnterCallback);
            glfwSetCursorPosCallback(vd.window, ImGuiImplGlfw.this::cursorPosCallback);
            glfwSetMouseButtonCallback(vd.window, ImGuiImplGlfw.this::mouseButtonCallback);
            glfwSetScrollCallback(vd.window, ImGuiImplGlfw.this::scrollCallback);
            glfwSetKeyCallback(vd.window, ImGuiImplGlfw.this::keyCallback);
            glfwSetCharCallback(vd.window, ImGuiImplGlfw.this::charCallback);
            glfwSetWindowCloseCallback(vd.window, ImGuiImplGlfw.this::windowCloseCallback);
            glfwSetWindowPosCallback(vd.window, ImGuiImplGlfw.this::windowPosCallback);
            glfwSetWindowSizeCallback(vd.window, ImGuiImplGlfw.this::windowSizeCallback);

            glfwMakeContextCurrent(vd.window);
            glfwSwapInterval(0);
        }
    }

    private final class DestroyWindowFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl.call/*extends ImPlatformFuncViewport*/ {

        @Override
        public void call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));

            if (vd != null && vd.windowOwned) {
                if (!glfwHasMousePassthrough && glfwHasWindowHovered && IS_WINDOWS) {
                    // TODO: RemovePropA
                }

                // Release any keys that were pressed in the window being destroyed and are still held down,
                // because we will not receive any release events after window is destroyed.
                for (int i = 0; i < data.keyOwnerWindows.length; i++) {
                    if (data.keyOwnerWindows[i] == vd.window) {
                        keyCallback(vd.window, i, 0, GLFW_RELEASE, 0); // Later params are only used for main viewport, on which this function is never called.
                    }
                }

                Callbacks.glfwFreeCallbacks(vd.window);
                glfwDestroyWindow(vd.window);

                vd.window = -1;
            }


            vp.setPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(-1));
            vp.setPlatformUserData(null);
        }
    }

    private static final class ShowWindowFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl.call/*extends ImPlatformFuncViewport*/ {

        @Override
        public void call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd == null) {
                return;
            }

//            if (IS_WINDOWS && vp.hasFlags(ImGuiViewportFlags_.NoTaskBarIcon)) { //Not possible using theese bindings
//                ImGuiImplGlfwNative.win32hideFromTaskBar(vp.getPlatformHandleRaw());
//            }

            glfwShowWindow(vd.window);
        }
    }

    private static final class GetWindowPosFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__ImVec2Impl.call/*extends ImPlatformFuncViewportSuppImVec2*/ {
        private final int[] posX = new int[1];
        private final int[] posY = new int[1];

//        @Override
//        public void get(final ImGuiViewport vp, final ImVec2 dst) {
//
//        }
        final ImVec2 result = new ImVec2();
        @Override
        public MemorySegment call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd == null) {
                return MemorySegment.ofAddress(ImGuiUtil.getImVec2(result));
            }
            posX[0] = 0;
            posY[0] = 0;
            glfwGetWindowPos(vd.window, posX, posY);
            result.setX(posX[0]);
            result.setY(posY[0]);
//            dst.set(posX[0], posY[0]);
            return MemorySegment.ofAddress(ImGuiUtil.getImVec2(result));
        }
    }

    private static final class SetWindowPosFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__voidImpl.call/*extends ImPlatformFuncViewportImVec2*/ {
//        @Override
//        public void accept(final ImGuiViewport vp, final ImVec2 value) {
//
//        }

        @Override
        public void call(MemorySegment a, MemorySegment b) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            var value = ImGuiUtil.newImVec2(b.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd == null) {
                return;
            }
            vd.ignoreWindowPosEventFrame = ImGui.GetFrameCount();
            glfwSetWindowPos(vd.window, (int) value.getX(), (int) value.getY());
        }
    }

    private static final class GetWindowSizeFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__ImVec2Impl.call/*extends ImPlatformFuncViewportSuppImVec2*/ {
        private final int[] width = new int[1];
        private final int[] height = new int[1];

//        @Override
//        public void get(final ImGuiViewport vp, final ImVec2 dst) {
//
//        }

        final ImVec2 result = new ImVec2();
        @Override
        public MemorySegment call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd == null) {
                return MemorySegment.ofAddress(ImGuiUtil.getImVec2(result));
            }
            width[0] = 0;
            height[0] = 0;
            glfwGetWindowSize(vd.window, width, height);
            result.setX(width[0]);
            result.setY(height[0]);
            return MemorySegment.ofAddress(ImGuiUtil.getImVec2(result));
        }
    }

    private final class SetWindowSizeFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__voidImpl.call/*extends ImPlatformFuncViewportImVec2*/ {
        private final int[] x = new int[1];
        private final int[] y = new int[1];
        private final int[] width = new int[1];
        private final int[] height = new int[1];

//        @Override
//        public void accept(final ImGuiViewport vp, final ImVec2 value) {
//
//        }

        @Override
        public void call(MemorySegment a, MemorySegment b) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            var value = ImGuiUtil.newImVec2(b.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd == null) {
                return;
            }
            if (IS_APPLE && !glfwHasOsxWindowPosFix) {
                // Native OS windows are positioned from the bottom-left corner on macOS, whereas on other platforms they are
                // positioned from the upper-left corner. GLFW makes an effort to convert macOS style coordinates, however it
                // doesn't handle it when changing size. We are manually moving the window in order for changes of size to be based
                // on the upper-left corner.
                x[0] = 0;
                y[0] = 0;
                width[0] = 0;
                height[0] = 0;
                glfwGetWindowPos(vd.window, x, y);
                glfwGetWindowSize(vd.window, width, height);
                glfwSetWindowPos(vd.window, x[0], y[0] - height[0] + (int) value.getY());
            }
            vd.ignoreWindowSizeEventFrame = ImGui.GetFrameCount();
            glfwSetWindowSize(vd.window, (int) value.getX(), (int) value.getY());
        }
    }

    private static final class SetWindowTitleFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_p_q_const__char__voidImpl.call/*extends ImPlatformFuncViewportString*/ {
//        @Override
//        public void accept(final ImGuiViewport vp, final String value) {
//
//        }

        @Override
        public void call(MemorySegment a, MemorySegment b) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd != null) {
                glfwSetWindowTitle(vd.window, b.getString(0));
            }
        }
    }

    private final class SetWindowFocusFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl.call/*extends ImPlatformFuncViewport*/ {
//        @Override
//        public void accept(final ImGuiViewport vp) {
//
//        }

        @Override
        public void call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            if (glfwHasFocusWindow) {
                final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
                if (vd != null) {
                    glfwFocusWindow(vd.window);
                }
            }
        }
    }

    private static final class GetWindowFocusFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__boolImpl.call/* extends ImPlatformFuncViewportSuppBoolean*/ {
//        @Override
//        public boolean get(final ImGuiViewport vp) {
//
//        }

        @Override
        public boolean call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData data = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            return glfwGetWindowAttrib(data.window, GLFW_FOCUSED) != 0;
        }
    }

    private static final class GetWindowMinimizedFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__boolImpl.call/*extends ImPlatformFuncViewportSuppBoolean*/ {
//        @Override
//        public boolean get(final ImGuiViewport vp) {
//
//        }

        @Override
        public boolean call(MemorySegment a) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd != null) {
                return glfwGetWindowAttrib(vd.window, GLFW_ICONIFIED) != GLFW_FALSE;
            }
            return false;
        }
    }

    private final class SetWindowAlphaFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_float__voidImpl.call/*extends ImPlatformFuncViewportFloat*/ {
//        @Override
//        public void accept(final ImGuiViewport vp, final float value) {
//
//        }

        @Override
        public void call(MemorySegment a, float value) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            if (glfwHasWindowAlpha) {
                final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
                if (vd != null) {
                    glfwSetWindowOpacity(vd.window, value);
                }
            }
        }
    }

    private static final class RenderWindowFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_p_void__voidImpl.call/*extends ImPlatformFuncViewport*/ {
//        @Override
//        public void accept(final ImGuiViewport vp) {
//
//        }

        @Override
        public void call(MemorySegment a, MemorySegment b) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd != null) {
                glfwMakeContextCurrent(vd.window);
            }
        }
    }

    private static final class SwapBuffersFunction implements ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_p_void__voidImpl.call/*extends ImPlatformFuncViewport*/ {
        @Override
        public void call(final MemorySegment a, final MemorySegment b) {
            var vp = ImGuiUtil.newImGuiViewport(a.address());
            final ViewportData vd = getUserData(ImGuiUtil.getSWIGTYPE_p_void(vp.getPlatformUserData()));
            if (vd != null) {
                glfwMakeContextCurrent(vd.window);
                glfwSwapBuffers(vd.window);
            }
        }
    }

    protected void initPlatformInterface() {
        final ImGuiPlatformIO platformIO = ImGui.GetPlatformIO();

        // Register platform interface (will be coupled with a renderer interface)
        platformIO.setPlatform_CreateWindow(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl(new CreateWindowFunction(), Arena.global()));
        platformIO.setPlatform_DestroyWindow(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl(new DestroyWindowFunction(), Arena.global()));
        platformIO.setPlatform_ShowWindow(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl(new ShowWindowFunction(), Arena.global()));
        platformIO.setPlatform_GetWindowPos(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__ImVec2Impl(new GetWindowPosFunction(), Arena.global()));
        platformIO.setPlatform_SetWindowPos(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__voidImpl(new SetWindowPosFunction(), Arena.global()));
        platformIO.setPlatform_GetWindowSize(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__ImVec2Impl(new GetWindowSizeFunction(), Arena.global()));
        platformIO.setPlatform_SetWindowSize(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__voidImpl(new SetWindowSizeFunction(), Arena.global()));
        platformIO.setPlatform_SetWindowTitle(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_p_q_const__char__voidImpl(new SetWindowTitleFunction(), Arena.global()));
        platformIO.setPlatform_SetWindowFocus(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__voidImpl(new SetWindowFocusFunction(), Arena.global()));
        platformIO.setPlatform_GetWindowFocus(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__boolImpl(new GetWindowFocusFunction(), Arena.global()));
        platformIO.setPlatform_GetWindowMinimized(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport__boolImpl(new GetWindowMinimizedFunction(), Arena.global()));
        platformIO.setPlatform_SetWindowAlpha(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_float__voidImpl(new SetWindowAlphaFunction(), Arena.global()));
        platformIO.setPlatform_RenderWindow(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_p_void__voidImpl(new RenderWindowFunction(), Arena.global()));
        platformIO.setPlatform_SwapBuffers(new ImGuiUtil.SWIGTYPE_p_f_p_ImGuiViewport_p_void__voidImpl(new SwapBuffersFunction(), Arena.global()));


        // Register main window handle (which is owned by the main application, not by us)
        // This is mostly for simplicity and consistency, so that our code (e.g. mouse handling etc.) can use same logic for main and secondary viewports.
        final ImGuiViewport mainViewport = ImGui.GetMainViewport();
        final ViewportData vd = new ViewportData();
        final long vd_ = putUserData(vd);
        vd.window = data.window;
        vd.windowOwned = false;
        mainViewport.setPlatformUserData(ImGuiUtil.newSWIGTYPE_p_void(vd_));
        mainViewport.setPlatformHandle(ImGuiUtil.newSWIGTYPE_p_void(data.window));
    }

    protected void shutdownPlatformInterface() {
        ImGui.DestroyPlatformWindows();
    }
}
