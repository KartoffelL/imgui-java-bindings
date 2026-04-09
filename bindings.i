%module ImGui

%{
  //#include "imgui/imgui.h"
  //#include "imgui/imgui.h"
  //#include "ImGuiColorTextEdit/TextEditor.h"
  //#include "imgui/backends/imgui_impl_opengl3.h"
  
  #include "imconfig.h" //Custom configuration for builds
  
  // Dear ImGui files
  #include "imgui/imgui.cpp"
  #include "imgui/imgui_demo.cpp"
  #include "imgui/imgui_draw.cpp"
  #include "imgui/imgui_tables.cpp"
  #include "imgui/imgui_widgets.cpp"
  
  // Dear ImGui backend
  
  // #include "glfw/include/GLFW/glfw3.h" //Compiling glfw would make things a lot more complicated
  // #include "glfw/include/GLFW/glfw3native.h"
  
  #include "imgui/backends/imgui_impl_opengl3.cpp"
  // #include "imgui/backends/imgui_impl_glfw.cpp"
  
  //ImGui Font rendering related
  #include "imgui/misc/freetype/imgui_freetype.cpp"
  
  
  //ImGuiColorTextEdit files
  #include "ImGuiColorTextEdit/TextEditor.cpp"


%}

%include "enumtypeunsafe.swg"
%include "std_vector.i"
%include "std_string_view.i"
%include "std_string.i"
//%include "director.swg"
%include "various.i"

%javaconst(1);

/*
 * (Copied from various.i and adapted for usage with non-const non-unsigned char*)
 *
 * char *NIOBUFFER typemaps. 
 * This is for mapping Java nio buffers to C char arrays.
 * It is useful for performance critical code as it reduces the memory copy an marshaling overhead.
 * Note: The Java buffer has to be allocated with allocateDirect.
 *
 * Example usage wrapping:
 *   %apply char *NIOBUFFER { char *buf };
 *   void foo(char *buf);
 *  
 * Java usage:
 *   java.nio.ByteBuffer b = ByteBuffer.allocateDirect(20); 
 *   modulename.foo(b);
 */
%typemap(jni) char *NIOBUFFER "jobject"  
%typemap(jtype) char *NIOBUFFER "java.nio.ByteBuffer"  
%typemap(jstype) char *NIOBUFFER "java.nio.ByteBuffer"  
%typemap(javain,
  pre="  assert $javainput.isDirect() : \"Buffer must be allocated direct.\";") char *NIOBUFFER "$javainput"
%typemap(javaout) char *NIOBUFFER {  
  return $jnicall;  
}  
%typemap(in) char *NIOBUFFER {  
  $1 = (char *) JCALL1(GetDirectBufferAddress, jenv, $input); 
  if ($1 == NULL) {  
    SWIG_JavaThrowException(jenv, SWIG_JavaRuntimeException, "Unable to get address of a java.nio.ByteBuffer direct byte buffer. Buffer must be a direct buffer and not a non-direct buffer.");  
  }  
}  
%typemap(memberin) char *NIOBUFFER {  
  if ($input) {  
    $1 = $input;  
  } else {  
    $1 = 0;  
  }  
}  
%typemap(freearg) char *NIOBUFFER ""  

%apply char *NIOBUFFER { char *buf };







//STD::functions for the TextEditor

%{
#include <functional>
#include <string>
#include <string_view>
#include <vector>
#include "TextEditor.h"
%}

%inline %{

// --------------------------------------------------
// Generic helper (internal use)
// --------------------------------------------------
template<typename R, typename... Args>
static std::function<R(Args...)>* make_fn_ptr(R (*fn)(Args...)) {
    if (!fn) return nullptr;

    return new std::function<R(Args...)>(
        [fn](Args... args) -> R {
            return fn(args...);
        }
    );
}

// --------------------------------------------------
// 1. bool(unsigned int)
// --------------------------------------------------
std::function<bool(unsigned int)>* make_fn_bool_uint(void* fnPtr) {
    using Fn = bool (*)(unsigned int);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 2. std::string(std::string_view)
// --------------------------------------------------
std::function<std::string(std::string_view)>* make_fn_string_stringview(void* fnPtr) {
    using Fn = std::string (*)(std::string_view);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 3. Iterator(Iterator, Iterator, Color&)
// --------------------------------------------------
std::function<TextEditor::Iterator(
    TextEditor::Iterator,
    TextEditor::Iterator,
    TextEditor::Color&
)>* make_fn_tokenizer(void* fnPtr) {

    using Fn = TextEditor::Iterator (*)(
        TextEditor::Iterator,
        TextEditor::Iterator,
        TextEditor::Color&
    );

    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 4. Iterator(Iterator, Iterator)
// --------------------------------------------------
std::function<TextEditor::Iterator(
    TextEditor::Iterator,
    TextEditor::Iterator
)>* make_fn_iterator_pair(void* fnPtr) {

    using Fn = TextEditor::Iterator (*)(
        TextEditor::Iterator,
        TextEditor::Iterator
    );

    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 5. void()
// --------------------------------------------------
std::function<void()>* make_fn_void(void* fnPtr) {
    using Fn = void (*)();
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 6. void(int, int)
// --------------------------------------------------
std::function<void(int, int)>* make_fn_void_int_int(void* fnPtr) {
    using Fn = void (*)(int, int);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 7. void(int, void*)
// --------------------------------------------------
std::function<void(int, void*)>* make_fn_void_int_ptr(void* fnPtr) {
    using Fn = void (*)(int, void*);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 8. void(int)
// --------------------------------------------------
std::function<void(int)>* make_fn_void_int(void* fnPtr) {
    using Fn = void (*)(int);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 9. void(const std::string&)
// --------------------------------------------------
std::function<void(const std::string&)>* make_fn_void_string_ref(void* fnPtr) {
    using Fn = void (*)(const std::string&);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 10. void(const std::vector<TextEditor::Change>&)
// --------------------------------------------------
std::function<void(const std::vector<TextEditor::Change>&)>*
make_fn_void_change_vector_ref(void* fnPtr) {

    using Fn = void (*)(const std::vector<TextEditor::Change>&);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 11. void(TextEditor::AutoCompleteState&)
// --------------------------------------------------
std::function<void(TextEditor::AutoCompleteState&)>*
make_fn_void_autocomplete_ref(void* fnPtr) {

    using Fn = void (*)(TextEditor::AutoCompleteState&);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 12. void(TextEditor::Decorator&)
// --------------------------------------------------
std::function<void(TextEditor::Decorator&)>*
make_fn_void_decorator_ref(void* fnPtr) {

    using Fn = void (*)(TextEditor::Decorator&);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

// --------------------------------------------------
// 13. void(*)(int)  (function pointer parameter)
// --------------------------------------------------
std::function<void(int)>* make_fn_void_fn_int(void* fnPtr) {
    using Fn = void (*)(int);
    return make_fn_ptr(reinterpret_cast<Fn>(fnPtr));
}

%}










%ignore TextV;
%ignore TextColoredV;
%ignore TextDisabledV;
%ignore TextWrappedV;
%ignore LabelTextV;
%ignore BulletTextV;
%ignore TreeNodeV;
%ignore TreeNodeExV;
%ignore SetTooltipV;
%ignore SetItemTooltipV;
%ignore LogTextV;
%ignore DebugLogV;
%ignore appendfv;

%rename(ImGuiFreeType_SetAllocatorFunctions) ImGuiFreeType::SetAllocatorFunctions;

//Sadly, we have to mirror all defines of imconfig.h, since SWIG can't handle that.
#define IMGUI_ENABLE_FREETYPE_PLUTOSVG
#define IMGUI_ENABLE_FREETYPE
#define IMGUI_USE_WCHAR32
//#define IMGUI_DISABLE_OBSOLETE_FUNCTIONS //TODO

////Includes------

%include "imgui/imgui.h"
%include "imgui/backends/imgui_impl_opengl3.h"

%include "ImGuiColorTextEdit/TextEditor.h"

%include "imgui/misc/freetype/imgui_freetype.h"

