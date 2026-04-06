package imgui.app;

import imguijb.*;
import org.lwjgl.system.Library;
import org.lwjgl.system.MemoryUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.ValueLayout;

public class ImUtil {



    static {
//        Library.loadSystem(System::load, System::loadLibrary, imgui.class, "imgui-java-bindings", Platform.mapLibraryNameBundled("imgui"));
        Library.loadSystem("imgui-java-binding", "imgui"); //Load native library via LWJGL's loading mechanism
        ImGui.CreateContext();
    }

    private static ImVec2[] vcs = new ImVec2[16];
    private static int i = 0;
    static {
        for(int i = 0; i < vcs.length; i++)
            vcs[i] = new ImVec2();
    }

    /**
     * Returns a cached ImVec2 that "is valid for the current invokation". This is meant to be invoked in functions like
     * Begin to avoid generating too many objects.
     * @param x the x
     * @param y the y
     * @return
     */
    public static ImVec2 ImVec2(float x, float y) {
        i++;
        if(i == vcs.length)
            i = 0;
        var r = vcs[i];
        r.setX(x);
        r.setY(y);
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
