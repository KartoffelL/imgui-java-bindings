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



%include "imgui/imgui.h"
%include "imgui/backends/imgui_impl_opengl3.h"
%include "imgui/misc/freetype/imgui_freetype.h"
%include "ImGuiColorTextEdit/TextEditor.h"
