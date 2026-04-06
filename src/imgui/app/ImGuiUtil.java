package imgui.app;

import imguijb.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * Class containing callbacks. The argument / return types should be inferred from the class name (thanks, SWIG)
 */
public class ImGuiUtil {


    public static final MemoryLayout ImVec2_ = MemoryLayout.structLayout(ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT);
    public static final int ImVec2__ = Math.toIntExact(ImVec2_.byteSize());

    public static final MemoryLayout ImGuiPlatformMonitor_ = MemoryLayout.structLayout(ImVec2_, ImVec2_, ValueLayout.JAVA_FLOAT, MemoryLayout.paddingLayout(4), ValueLayout.ADDRESS);

    public static final MemoryLayout ImDrawVert_ = MemoryLayout.structLayout(ImVec2_, ImVec2_, ValueLayout.JAVA_INT);
    public static final int ImDrawVert__ = Math.toIntExact(ImDrawVert_.byteSize());

    public static final int ImDrawIDX__ = Math.toIntExact(ValueLayout.JAVA_SHORT.byteSize());

    public static final MemoryLayout C_INT = Linker.nativeLinker().canonicalLayouts().get("int");
    {
        if(C_INT.byteSize() != ValueLayout.JAVA_INT.byteSize())
            throw new IllegalStateException("Cint is not the same as Java int! cint " + C_INT + "vs jint " + ValueLayout.JAVA_INT);
    }


    public static class ImDrawCmdVector extends ImVector<ImDrawCmd> {

        private static class ImDrawCmdImpl extends ImDrawCmd {
            public ImDrawCmdImpl(long ptr) {
                super(ptr, false);
            }
            public static long get(ImDrawCmd a) {
                return getCPtr(a);
            }
        }

        private static class SWIGTYPE_p_ImVectorT_ImDrawCmd_tImpl extends SWIGTYPE_p_ImVectorT_ImDrawCmd_t {
            public static long get(SWIGTYPE_p_ImVectorT_ImDrawCmd_t a) {
                return getCPtr(a);
            }
        }

        public ImDrawCmdVector(SWIGTYPE_p_ImVectorT_ImDrawCmd_t ptr) {
            super(SWIGTYPE_p_ImVectorT_ImDrawCmd_tImpl.get(ptr));
        }

        public ImDrawCmdVector(long ptr) {
            super(ptr);
        }

        @Override
        protected ImDrawCmd read(MemorySegment base, int index) {
            long offset = (long) index * elementSize();
            MemorySegment ptr = base.get(ValueLayout.ADDRESS, offset);
            if (ptr == MemorySegment.NULL) return null;
            return new ImDrawCmdImpl(ptr.address());
        }

        @Override
        protected void write(MemorySegment base, int index, ImDrawCmd value) {
            long offset = (long) index * elementSize();
            MemorySegment ptr = (value == null) ? MemorySegment.NULL : MemorySegment.ofAddress(ImDrawCmdImpl.get(value));
            base.set(ValueLayout.ADDRESS, offset, ptr);
        }

        @Override
        protected long elementSize() {
            return ValueLayout.ADDRESS.byteSize();
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            MemorySegment newData = MemorySegment.ofAddress(getSWIGTYPE_p_void(imgui.MemAlloc(newCapacity * elementSize())))
                    .reinterpret(newCapacity * elementSize());
            if (oldData != null) {
                long bytesToCopy = (long) oldCapacity * elementSize();
                MemorySegment.copy(oldData, 0, newData, 0, bytesToCopy);
            }
            return newData;
        }
    }


    public static class ImDrawListVector extends ImVector<ImDrawList> {

        private static class ImDrawListImpl extends ImDrawList {
            public ImDrawListImpl(long ptr) {
                super(ptr, false);
            }
            public static long get(ImDrawList a) {
                return getCPtr(a);
            }
        }

        private static class SWIGTYPE_p_ImVectorT_ImDrawList_p_tImpl extends SWIGTYPE_p_ImVectorT_ImDrawList_p_t {
            public static long get(SWIGTYPE_p_ImVectorT_ImDrawList_p_t a) {
                return getCPtr(a);
            }
        }

        public ImDrawListVector(SWIGTYPE_p_ImVectorT_ImDrawList_p_t ptr) {
            super(SWIGTYPE_p_ImVectorT_ImDrawList_p_tImpl.get(ptr));
        }

        public ImDrawListVector(long ptr) {
            super(ptr);
        }

        @Override
        protected ImDrawList read(MemorySegment base, int index) {
            long offset = (long) index * elementSize();
            MemorySegment ptr = base.get(ValueLayout.ADDRESS, offset);
            if (ptr == MemorySegment.NULL) return null;
            return new ImDrawListImpl(ptr.address());
        }

        @Override
        protected void write(MemorySegment base, int index, ImDrawList value) {
            long offset = (long) index * elementSize();
            MemorySegment ptr = (value == null) ? MemorySegment.NULL : MemorySegment.ofAddress(ImDrawListImpl.get(value));
            base.set(ValueLayout.ADDRESS, offset, ptr);
        }

        @Override
        protected long elementSize() {
            return ValueLayout.ADDRESS.byteSize();
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            MemorySegment newData = MemorySegment.ofAddress(getSWIGTYPE_p_void(imgui.MemAlloc(newCapacity * elementSize())))
                    .reinterpret(newCapacity * elementSize());
            if (oldData != null) {
                long bytesToCopy = (long) oldCapacity * elementSize();
                MemorySegment.copy(oldData, 0, newData, 0, bytesToCopy);
            }
            return newData;
        }
    }

    public static class ImTextureRectVector extends ImVector<ImTextureRect> {

        private static class ImTextureRectImpl extends ImTextureRect {
            public ImTextureRectImpl(long ptr) {
                super(ptr, false);
            }
            public static long get(ImTextureRect a) {
                return getCPtr(a);
            }
        }

        private static class SWIGTYPE_p_ImVectorT_ImTextureRect_tImpl extends SWIGTYPE_p_ImVectorT_ImTextureRect_t {
            public static long get(SWIGTYPE_p_ImVectorT_ImTextureRect_t a) {
                return getCPtr(a);
            }
        }

        public ImTextureRectVector(SWIGTYPE_p_ImVectorT_ImTextureRect_t ptr) {
            super(SWIGTYPE_p_ImVectorT_ImTextureRect_tImpl.get(ptr));
        }

        public ImTextureRectVector(long ptr) {
            super(ptr);
        }

        @Override
        protected ImTextureRect read(MemorySegment base, int index) {
            long offset = (long) index * elementSize();
            MemorySegment ptr = base.get(ValueLayout.ADDRESS, offset);
            if (ptr == MemorySegment.NULL) return null;
            return new ImTextureRectImpl(ptr.address());
        }

        @Override
        protected void write(MemorySegment base, int index, ImTextureRect value) {
            long offset = (long) index * elementSize();
            MemorySegment ptr = (value == null) ? MemorySegment.NULL : MemorySegment.ofAddress(ImTextureRectImpl.get(value));
            base.set(ValueLayout.ADDRESS, offset, ptr);
        }

        @Override
        protected long elementSize() {
            return ValueLayout.ADDRESS.byteSize();
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            MemorySegment newData = MemorySegment.ofAddress(getSWIGTYPE_p_void(imgui.MemAlloc(newCapacity * elementSize())))
                    .reinterpret(newCapacity * elementSize());
            if (oldData != null) {
                long bytesToCopy = (long) oldCapacity * elementSize();
                MemorySegment.copy(oldData, 0, newData, 0, bytesToCopy);
            }
            return newData;
        }
    }

    public static class ImGuiPlatformMonitorVector extends ImVector<ImGuiPlatformMonitor> {

        private static class ImGuiPlatformMonitorImpl extends ImGuiPlatformMonitor {
            public ImGuiPlatformMonitorImpl(long ptr) {
                super(ptr, false); // SWIG-style constructor
            }
            public static long get(ImGuiPlatformMonitor a) {
                return getCPtr(a);
            }
        }

        private static class SWIGTYPE_p_ImVectorT_ImGuiPlatformMonitor_tImpl extends SWIGTYPE_p_ImVectorT_ImGuiPlatformMonitor_t {
            public static long get(SWIGTYPE_p_ImVectorT_ImGuiPlatformMonitor_t a) {
                return getCPtr(a);
            }
        }

        public ImGuiPlatformMonitorVector(SWIGTYPE_p_ImVectorT_ImGuiPlatformMonitor_t ptr) {
            super(SWIGTYPE_p_ImVectorT_ImGuiPlatformMonitor_tImpl.get(ptr));
        }

        public ImGuiPlatformMonitorVector(long ptr) {
            super(ptr);
        }

        @Override
        protected ImGuiPlatformMonitor read(MemorySegment base, int index) {
            long offset = (long) index * elementSize();
            MemorySegment slice = base.asSlice(offset, elementSize());
            return new ImGuiPlatformMonitorImpl(slice.address());
        }

        @Override
        protected void write(MemorySegment base, int index, ImGuiPlatformMonitor value) {
            long offset = (long) index * elementSize();
            MemorySegment dst = base.asSlice(offset, elementSize());
            MemorySegment src = MemorySegment.ofAddress(ImGuiPlatformMonitorImpl.get(value)).reinterpret(elementSize());
            MemorySegment.copy(src, 0, dst, 0, elementSize());
        }

        @Override
        protected long elementSize() {
            return ImGuiPlatformMonitor_.byteSize(); // must match sizeof(ImGuiPlatformMonitor)
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            // ⚠️ Ideally call ImGui::MemAlloc if resizing ImGui-owned vectors
            MemorySegment newData = MemorySegment.ofAddress(getSWIGTYPE_p_void(imgui.MemAlloc(newCapacity * elementSize())))
                    .reinterpret(newCapacity * elementSize());

            if (oldData != null) {
                long bytesToCopy = (long) oldCapacity * elementSize();
                MemorySegment.copy(oldData, 0, newData, 0, bytesToCopy);
            }

            return newData;
        }
    }

    public static class ImGuiViewportVector extends ImVector<ImGuiViewport> {

        private static class ImGuiViewportImpl extends ImGuiViewport {
            public ImGuiViewportImpl(long pointr) {
                super(pointr, false);
            }
            public static long get(ImGuiViewport a){
                return getCPtr(a);
            }
        }
        private static class SWIGTYPE_p_ImVectorT_ImGuiViewport_p_tImpl extends SWIGTYPE_p_ImVectorT_ImGuiViewport_p_t {
            public static long get(SWIGTYPE_p_ImVectorT_ImGuiViewport_p_t a){
                return getCPtr(a);
            }
        }

        public ImGuiViewportVector(SWIGTYPE_p_ImVectorT_ImGuiViewport_p_t ptr) {
            super(SWIGTYPE_p_ImVectorT_ImGuiViewport_p_tImpl.get(ptr));
        }

        public ImGuiViewportVector(long ptr) {
            super(ptr);
        }

        @Override
        protected ImGuiViewport read(MemorySegment base, int index) {
            long offset = (long) index * elementSize();

            MemorySegment ptr = base.get(ValueLayout.ADDRESS, offset);

            if (ptr == MemorySegment.NULL) {
                return null;
            }

            return new ImGuiViewportImpl(ptr.address()); // SWIG-style wrapper
        }

        @Override
        protected void write(MemorySegment base, int index, ImGuiViewport value) {
            long offset = (long) index * elementSize();

            MemorySegment ptr = (value == null)
                    ? MemorySegment.NULL
                    : MemorySegment.ofAddress(ImGuiViewportImpl.get(value));

            base.set(ValueLayout.ADDRESS, offset, ptr);
        }

        @Override
        protected long elementSize() {
            return ValueLayout.ADDRESS.byteSize(); // pointer size
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            MemorySegment newData = MemorySegment.ofAddress(getSWIGTYPE_p_void(imgui.MemAlloc(newCapacity * elementSize())))
                    .reinterpret(newCapacity * elementSize());

            if (oldData != null) {
                long bytesToCopy = (long) oldCapacity * elementSize();
                MemorySegment.copy(oldData, 0, newData, 0, bytesToCopy);
            }

            return newData;
        }
    }

    public static class ImGuiShortVector extends ImVector<Short> {

        private static class SWIGTYPE_p_ImVectorT_unsigned_short_tImpl extends SWIGTYPE_p_ImVectorT_unsigned_short_t {
            public static long get(SWIGTYPE_p_ImVectorT_unsigned_short_t a){
                return getCPtr(a);
            }
        }

        public ImGuiShortVector(SWIGTYPE_p_ImVectorT_unsigned_short_t ptr) {
            super(SWIGTYPE_p_ImVectorT_unsigned_short_tImpl.get(ptr));
        }

        public ImGuiShortVector(long ptr) {
            super(ptr);
        }

        @Override
        protected Short read(MemorySegment base, int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void write(MemorySegment base, int index, Short value) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected long elementSize() {
            return ValueLayout.JAVA_SHORT.byteSize(); // pointer size
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ImGuiImVertVector extends ImVector<ImDrawVert> {

        private static class SWIGTYPE_p_ImVectorT_ImDrawVert_tImpl extends SWIGTYPE_p_ImVectorT_ImDrawVert_t {
            public static long get(SWIGTYPE_p_ImVectorT_ImDrawVert_t a){
                return getCPtr(a);
            }
        }

        public ImGuiImVertVector(SWIGTYPE_p_ImVectorT_ImDrawVert_t ptr) {
            super(SWIGTYPE_p_ImVectorT_ImDrawVert_tImpl.get(ptr));
        }

        public ImGuiImVertVector(long ptr) {
            super(ptr);
        }

        @Override
        protected ImDrawVert read(MemorySegment base, int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void write(MemorySegment base, int index, ImDrawVert value) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected long elementSize() {
            return ImDrawVert__;
        }

        @Override
        protected MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity) {
            throw new UnsupportedOperationException();
        }
    }


    public static abstract class ImVector<T> implements Iterable<T> {
        protected final MemorySegment memory;
        private volatile int iteratorIndex = -1;
        private static final long OFFSET_SIZE = 0;
        private static final long OFFSET_CAPACITY = 4;
        private static final long OFFSET_DATA = 8;
        public ImVector(long pointer) {
            this.memory = MemorySegment.ofAddress(pointer)
                    .reinterpret(OFFSET_DATA + ValueLayout.ADDRESS.byteSize());
        }
        protected abstract T read(MemorySegment base, int index);
        protected abstract void write(MemorySegment base, int index, T value);
        protected abstract long elementSize();
        protected abstract MemorySegment reallocate(MemorySegment oldData, int oldCapacity, int newCapacity);
        public int size() {
            return memory.get(ValueLayout.JAVA_INT, OFFSET_SIZE);
        }
        public void size(int newSize) {
            memory.set(ValueLayout.JAVA_INT, OFFSET_SIZE, newSize);
        }
        public int capacity() {
            return memory.get(ValueLayout.JAVA_INT, OFFSET_CAPACITY);
        }
        public void capacity(int newCapacity) {
            memory.set(ValueLayout.JAVA_INT, OFFSET_CAPACITY, newCapacity);
        }
        public MemorySegment data() {
            return memory.get(ValueLayout.ADDRESS, OFFSET_DATA).reinterpret(size()*elementSize());
        }
        public void data(MemorySegment ptr) {
            memory.set(ValueLayout.ADDRESS, OFFSET_DATA, ptr);
        }
        public T get(int index) {
            checkIndex(index);
            return read(data(), index);
        }
        public void set(int index, T value) {
            if(iteratorIndex != -1)
                throw new ConcurrentModificationException();
            checkIndex(index);
            write(data(), index, value);
        }
        public void push(T value) {
            if(iteratorIndex != -1)
                throw new ConcurrentModificationException();
            var siz = size();
            resize(siz+1); //Increase by one
            set(siz, value);
        }
        public void resize(int newSize) {
            if(iteratorIndex != -1)
                throw new ConcurrentModificationException();
            int currentSize = size();
            int currentCapacity = capacity();

            if (newSize > currentCapacity) {
                grow(newSize);
            }
            size(newSize);
        }
        public void reserve(int newCapacity) {
            if(iteratorIndex != -1)
                throw new ConcurrentModificationException();
            if (newCapacity <= capacity()) return;
            grow(newCapacity);
        }
        private void grow(int minCapacity) {
            int oldCapacity = capacity();
            int newCapacity = Math.max(minCapacity, oldCapacity * 2);

            MemorySegment oldData = data();
            MemorySegment newData = reallocate(oldData, oldCapacity, newCapacity);

            data(newData);
            capacity(newCapacity);
        }
        private void checkIndex(int index) {
            if (index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException(index);
            }
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                int index = -1;
                @Override
                public boolean hasNext() {
                    return index < (size()-1);
                }

                @Override
                public T next() {
                    index++;
                    return get(index);
                }
            };
        }
    }

//    private static class SWIGTYPE_p_ImVectorT_unsigned_short_tImpl extends SWIGTYPE_p_ImVectorT_unsigned_short_t {
//        public SWIGTYPE_p_ImVectorT_unsigned_short_tImpl(long pointer) {super(pointer, false);}
//        public static long get(SWIGTYPE_p_ImVectorT_unsigned_short_t p) {
//            return SWIGTYPE_p_ImVectorT_unsigned_short_t.getCPtr(p);
//        }
//    }
//    public static SWIGTYPE_p_ImVectorT_unsigned_short_t newSWIGTYPE_p_ImVectorT_unsigned_short_t(long pointer) {
//        return new SWIGTYPE_p_ImVectorT_unsigned_short_tImpl(pointer);
//    }
//    public static long getSWIGTYPE_p_ImVectorT_unsigned_short_t(SWIGTYPE_p_ImVectorT_unsigned_short_t object) {
//        return SWIGTYPE_p_ImVectorT_unsigned_short_tImpl.get(object);
//    }
//
//    private static class SWIGTYPE_p_ImVectorT_ImDrawVert_tImpl extends SWIGTYPE_p_ImVectorT_ImDrawVert_t {
//        public SWIGTYPE_p_ImVectorT_ImDrawVert_tImpl(long pointer) {super(pointer, false);}
//        public static long get(SWIGTYPE_p_ImVectorT_ImDrawVert_t p) {
//            return SWIGTYPE_p_ImVectorT_ImDrawVert_t.getCPtr(p);
//        }
//    }
//    public static SWIGTYPE_p_ImVectorT_ImDrawVert_t newSWIGTYPE_p_ImVectorT_ImDrawVert_t(long pointer) {
//        return new SWIGTYPE_p_ImVectorT_ImDrawVert_tImpl(pointer);
//    }
//    public static long getSWIGTYPE_p_ImVectorT_ImDrawVert_t(SWIGTYPE_p_ImVectorT_ImDrawVert_t object) {
//        return SWIGTYPE_p_ImVectorT_ImDrawVert_tImpl.get(object);
//    }

    private static class ImVec2Impl extends ImVec2 {
        public ImVec2Impl(long pointer) {super(pointer, false);}
        public static long get(ImVec2 p) {
            return ImVec2.getCPtr(p);
        }
    }
    public static ImVec2 newImVec2(long pointer) {
        return new ImVec2Impl(pointer);
    }
    public static long getImVec2(ImVec2 object) {
        return ImVec2Impl.get(object);
    }

    private static class ImGuiViewportImpl extends ImGuiViewport {
        public ImGuiViewportImpl(long pointer) {super(pointer, false);}
        public static long get(ImGuiViewport p) {
            return ImGuiViewport.getCPtr(p);
        }
    }
    public static ImGuiViewport newImGuiViewport(long pointer) {
        return new ImGuiViewportImpl(pointer);
    }
    public static long getImGuiViewport(SWIGTYPE_p_void object) {
        return SWIGTYPE_p_voidImpl.get(object);
    }

    private static class SWIGTYPE_p_voidImpl extends SWIGTYPE_p_void {
        public SWIGTYPE_p_voidImpl(long pointer) {super(pointer, false);}
        public static long get(SWIGTYPE_p_void p) {
            return SWIGTYPE_p_void.getCPtr(p);
        }
    }
    public static SWIGTYPE_p_void newSWIGTYPE_p_void(long pointer) {
        return new SWIGTYPE_p_voidImpl(pointer);
    }
    public static long getSWIGTYPE_p_void(SWIGTYPE_p_void object) {
        return SWIGTYPE_p_voidImpl.get(object);
    }

    public static class SWIGTYPE_p_f_p_void_p_q_const__char__voidImpl extends SWIGTYPE_p_f_p_void_p_q_const__char__void implements AutoCloseable{
        @FunctionalInterface public interface call {
            MemorySegment call(MemorySegment arg1, MemorySegment arg2);
        }
        private final Arena arena;
        private final MemorySegment segment;
        public SWIGTYPE_p_f_p_void_p_q_const__char__voidImpl(call call, Arena arena){
            FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle mh;
            try {
                mh = MethodHandles.lookup().findVirtual(call.class,"call",
                        MethodType.methodType(MemorySegment.class, MemorySegment.class, MemorySegment.class)
                ).bindTo(call);
            } catch (Exception e) {throw new RuntimeException(e);}
            var segment = Linker.nativeLinker().upcallStub(mh,fd,arena);
            super(segment.address(), false);this.segment = segment;this.arena = arena;
        }
        public Arena getArena() {return arena;}
        public MemorySegment getSegment() {return segment;}
        public void close() {arena.close();}
    }

    public static class SWIGTYPE_p_f_p_void__p_charImpl extends SWIGTYPE_p_f_p_void__p_char implements AutoCloseable{
        @FunctionalInterface public interface call {
            MemorySegment call(MemorySegment arg1);
        }
        private final Arena arena;
        private final MemorySegment segment;
        public SWIGTYPE_p_f_p_void__p_charImpl(call call, Arena arena){
            FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
            MethodHandle mh;
            try {
                mh = MethodHandles.lookup().findVirtual(call.class,"call",
                        MethodType.methodType(MemorySegment.class, MemorySegment.class)
                ).bindTo(call);
            } catch (Exception e) {throw new RuntimeException(e);}
            var segment = Linker.nativeLinker().upcallStub(mh,fd,arena);
            super(segment.address(), false);this.segment = segment;this.arena = arena;
        }
        public Arena getArena() {return arena;}
        public MemorySegment getSegment() {return segment;}
        public void close() {arena.close();}
    }

    private interface b<T> {
        T c() throws Exception;
    }

    private static <T> T a(b<T> f) {
        try {
            return f.c();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport__voidImpl extends SWIGTYPE_p_f_p_ImGuiViewport__void implements AutoCloseable{
        @FunctionalInterface public interface call{void call(MemorySegment a);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport__voidImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(void.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__voidImpl extends SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__void implements AutoCloseable{
        @FunctionalInterface public interface call{void call(MemorySegment a,MemorySegment b);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport_ImVec2__voidImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(void.class,MemorySegment.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport__ImVec2Impl extends SWIGTYPE_p_f_p_ImGuiViewport__ImVec2 implements AutoCloseable{
        @FunctionalInterface public interface call{MemorySegment call(MemorySegment a);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport__ImVec2Impl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(MemorySegment.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.of(ValueLayout.ADDRESS,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport_p_q_const__char__voidImpl extends SWIGTYPE_p_f_p_ImGuiViewport_p_q_const__char__void implements AutoCloseable{
        @FunctionalInterface public interface call{void call(MemorySegment a,MemorySegment b);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport_p_q_const__char__voidImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(void.class,MemorySegment.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport_float__voidImpl extends SWIGTYPE_p_f_p_ImGuiViewport_float__void implements AutoCloseable{
        @FunctionalInterface public interface call{void call(MemorySegment a,float b);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport_float__voidImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(void.class,MemorySegment.class,float.class))).bindTo(c),FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.JAVA_FLOAT),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport__boolImpl extends SWIGTYPE_p_f_p_ImGuiViewport__bool implements AutoCloseable{
        @FunctionalInterface public interface call{boolean call(MemorySegment a);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport__boolImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(boolean.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport_p_void__voidImpl extends SWIGTYPE_p_f_p_ImGuiViewport_p_void__void implements AutoCloseable{
        @FunctionalInterface public interface call{void call(MemorySegment a,MemorySegment b);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport_p_void__voidImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(void.class,MemorySegment.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.ofVoid(ValueLayout.ADDRESS,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport__floatImpl extends SWIGTYPE_p_f_p_ImGuiViewport__float implements AutoCloseable{
        @FunctionalInterface public interface call{float call(MemorySegment a);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport__floatImpl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(float.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.of(ValueLayout.JAVA_FLOAT,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }

    public static class SWIGTYPE_p_f_p_ImGuiViewport__ImVec4Impl extends SWIGTYPE_p_f_p_ImGuiViewport__ImVec4 implements AutoCloseable{
        @FunctionalInterface public interface call{MemorySegment call(MemorySegment a);}
        private final Arena arena; private final MemorySegment segment;
        public SWIGTYPE_p_f_p_ImGuiViewport__ImVec4Impl(call c,Arena ar){super(Linker.nativeLinker().upcallStub(a(()->MethodHandles.lookup().findVirtual(call.class,"call",MethodType.methodType(MemorySegment.class,MemorySegment.class))).bindTo(c),FunctionDescriptor.of(ValueLayout.ADDRESS,ValueLayout.ADDRESS),ar).address(),false);segment=null;arena=ar;}
        public Arena getArena(){return arena;} public MemorySegment getSegment(){return segment;} public void close(){arena.close();}
    }










}

