package imgui.app;

import imguijb.*;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;

/*
 * This Class contains some often used functions when using the ImGui library.
 * Loading this class will also statically load the native libraries.
 *
*/
public class ImUtil {



    static {
//        Library.loadSystem(System::load, System::loadLibrary, imgui.class, "imgui-java-bindings", Platform.mapLibraryNameBundled("imgui"));
        Library.loadSystem("imgui-java-binding", "imgui"); //Load native library via LWJGL's loading mechanism
        ImGui.CreateContext();
    }

    private final static ImVec2[] vc2s = new ImVec2[16];
    private static int vc2sI = 0;
    static {
        for(int i = 0; i < vc2s.length; i++)
            vc2s[i] = new ImVec2();
    }

    /**
     * Returns a cached ImVec2 that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param x the x
     * @param y the y
     * @return
     */
    public static ImVec2 ImVec2(float x, float y) {
        vc2sI++;
        if(vc2sI == vc2s.length)
            vc2sI = 0;
        var r = vc2s[vc2sI];
        r.setX(x);
        r.setY(y);
        return r;
    }

    private final static ImVec4[] vc4s = new ImVec4[16];
    private static int vc4sI = 0;
    static {
        for(int i = 0; i < vc4s.length; i++)
            vc4s[i] = new ImVec4();
    }
    

    /**
     * Returns a cached ImVec4 that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param x the x
     * @param y the y
     * @param z the z
     * @param w the w
     * @return
     */
    public static ImVec4 ImVec4(float x, float y, float z, float w) {
        vc4sI++;
        if(vc4sI == vc4s.length)
            vc4sI = 0;
        var r = vc4s[vc4sI];
        r.setX(x);
        r.setY(y);
        r.setZ(z);
        r.setW(w);
        return r;
    }

    private final static ImInt[] ints = new ImInt[16];
    private static int intsI = 0;
    static {
        for(int i = 0; i < ints.length; i++)
            ints[i] = new ImInt();
    }
    

    /**
     * Returns a cached ImInt that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param i the i
     * @return
     */
    public static ImInt ImInt(int i) {
        intsI++;
        if(intsI == ints.length)
            intsI = 0;
        var r = ints[intsI];
        r.put(i);
        return r;
    }

    // ---------- ImFloat ----------
    private final static ImFloat[] floats = new ImFloat[16];
    private static int floatsI = 0;
    static {
        for (int i = 0; i < floats.length; i++)
            floats[i] = new ImFloat();
    }
    
    /**
     * Returns a cached ImFloat that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param f the f
     * @return
     */
    public static ImFloat ImFloat(float f) {
        floatsI++;
        if (floatsI == floats.length)
            floatsI = 0;
        var r = floats[floatsI];
        r.put(f);
        return r;
    }
    
    
    // ---------- ImDouble ----------
    private final static ImDouble[] doubles = new ImDouble[16];
    private static int doublesI = 0;
    static {
        for (int i = 0; i < doubles.length; i++)
            doubles[i] = new ImDouble();
    }
    
    /**
     * Returns a cached ImDouble that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param d the d
     * @return
     */
    public static ImDouble ImDouble(double d) {
        doublesI++;
        if (doublesI == doubles.length)
            doublesI = 0;
        var r = doubles[doublesI];
        r.put(d);
        return r;
    }
    
    
    // ---------- ImByte ----------
    private final static ImByte[] bytes = new ImByte[16];
    private static int bytesI = 0;
    static {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = new ImByte();
    }
    
    /**
     * Returns a cached ImByte that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param b the b
     * @return
     */
    public static ImByte ImByte(byte b) {
        bytesI++;
        if (bytesI == bytes.length)
            bytesI = 0;
        var r = bytes[bytesI];
        r.put(b);
        return r;
    }
    
    
    // ---------- ImBoolean ----------
    private final static ImBool[] bools = new ImBool[16];
    private static int boolsI = 0;
    static {
        for (int i = 0; i < bools.length; i++)
            bools[i] = new ImBool();
    }
    
    /**
     * Returns a cached ImBool that "is valid for the current invocation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param b the b
     * @return
     */
    public static ImBool ImBool(boolean b) {
        boolsI++;
        if (boolsI == bools.length)
            boolsI = 0;
        var r = bools[boolsI];
        r.put(b);
        return r;
    }




    /**
     * Represents an ImGui Integer Pointer
     */
    public static class ImInt extends SWIGTYPE_p_int {
        private final Arena arena;
        public ImInt(long address, Arena arena, boolean owned){
            super(address, owned);
            this.arena = arena;
        }
        public ImInt(){
            var arena = Arena.ofAuto();
            super(arena.allocate(4).address(), true);
            this.arena = arena;
        }
        public ImInt(int value) {
            this();
            put(value);
        }
        public void put(int value) {
            MemoryUtil.memPutInt(getCPtr(this), value);
        }
        public int get() {
            return MemoryUtil.memGetInt(getCPtr(this));
        }

        public Arena getArena() {
            return arena;
        }
    }
    /**
     * Represents an ImGui Float Pointer
     */
    public static class ImFloat extends SWIGTYPE_p_float {
        private final Arena arena;
        public ImFloat(long address, Arena arena, boolean owned){
            super(address, owned);
            this.arena = arena;
        }
        public ImFloat(){
            var arena = Arena.ofAuto();
            super(arena.allocate(4).address(), true);
            this.arena = arena;
        }

        public ImFloat(float value) {
            this();
            put(value);
        }

        public void put(float value) {
            MemoryUtil.memPutFloat(getCPtr(this), value);
        }

        public float get() {
            return MemoryUtil.memGetFloat(getCPtr(this));
        }
        public Arena getArena() {
            return arena;
        }
    }
    /**
     * Represents an ImGui Double Pointer
     */
    public static class ImDouble extends SWIGTYPE_p_double{
        private final Arena arena;
        public ImDouble(long address, Arena arena, boolean owned){
            super(address, owned);
            this.arena = arena;
        }
        public ImDouble(){
            var arena = Arena.ofAuto();
            super(arena.allocate(8).address(), true);
            this.arena = arena;
        }

        public ImDouble(double value) {
            this();
            put(value);
        }

        public void put(double value) {
            MemoryUtil.memPutDouble(getCPtr(this), value);
        }

        public double get() {
            return MemoryUtil.memGetDouble(getCPtr(this));
        }
        public Arena getArena() {
            return arena;
        }
    }
    /**
     * Represents an ImGui Byte Pointer
     */
    public static class ImByte extends SWIGTYPE_p_p_char {
        private final Arena arena;
        public ImByte(long address, Arena arena, boolean owned){
            super(address, owned);
            this.arena = arena;
        }
        public ImByte(){
            var arena = Arena.ofAuto();
            super(arena.allocate(1).address(), true);
            this.arena = arena;
        }

        public ImByte(byte value) {
            this();
            put(value);
        }

        public void put(byte value) {
            MemoryUtil.memPutByte(getCPtr(this), value);
        }

        public byte get() {
            return MemoryUtil.memGetByte(getCPtr(this));
        }
        public Arena getArena() {
            return arena;
        }
    }
    /**
     * Represents an ImGui Boolean Pointer
     */
    public static class ImBool extends SWIGTYPE_p_bool {
        private final Arena arena;
        public ImBool(long address, Arena arena, boolean owned){
            super(address, owned);
            this.arena = arena;
        }
        public ImBool(){
            var arena = Arena.ofAuto();
            super(arena.allocate(1).address(), true);
            this.arena = arena;
        }

        public ImBool(boolean value) {
            this();
            put(value);
        }

        public void put(boolean value) {
            MemoryUtil.memPutByte(getCPtr(this), (byte)(value ? 1 : 0));
        }

        public boolean get() {
            return MemoryUtil.memGetByte(getCPtr(this)) != 0;
        }
        public Arena getArena() {
            return arena;
        }
    }

}
