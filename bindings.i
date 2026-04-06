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

// Tell SWIG to use ByteBuffer for char* input/output buffers
%typemap(jstype) char* "java.nio.ByteBuffer"
%typemap(jni) char* (java.nio.ByteBuffer buf) %{
    if (!buf.isDirect()) {
        SWIG_JavaThrowException(env, SWIG_JavaIllegalArgumentException, "ByteBuffer must be direct");
        return 0;
    }
    $jnicall = (char*) buf.address();
%}

// No javaout needed; changes happen in-place
%typemap(javaout) char* %{
    // nothing; direct ByteBuffer is already updated
%}

// Apply in/out pattern for buffers
%apply (char* INOUT, size_t BUFFER_SIZE) { (char* buf, size_t buf_size) };


%include "imgui/imgui.h"
%include "ImGuiColorTextEdit/TextEditor.h"
