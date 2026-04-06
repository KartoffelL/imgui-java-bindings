%module imgui

%{
#include "imgui/imgui.h"
#include "ImGuiColorTextEdit/TextEditor.h"
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

%include "enumtypeunsafe.swg"
%javaconst(1);

// Typemap to convert C++ char* in/out buffers to Java ByteBuffer
%typemap(jstype, in, out) char* "java.nio.ByteBuffer"

%typemap(jni, in) char* {
    if (!$input.isDirect()) {
        SWIG_JavaThrowException(env, SWIG_JavaIllegalArgumentException, "ByteBuffer must be direct");
        return 0;
    }
    $jnicall = (char*) $input.address();
}

%typemap(javaout) char* {
    // Nothing needed; ByteBuffer is updated in place
}

// Apply in/out handling with buffer size
%apply (char* INOUT, size_t BUFFER_SIZE) { (char* buf, size_t buf_size) };

%include "imgui/imgui.h"
%include "ImGuiColorTextEdit/TextEditor.h"
