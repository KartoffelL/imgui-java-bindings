%module imgui

%{
#include "imgui/imgui.h"
#include "ImGuiColorTextEdit/TextEditor.h"
%}

%include "enumtypeunsafe.swg"
%include "std_vector.i"

%javaconst(1);

%typemap(jni) char *, char *&, char[ANY], char[]               "jbyteArray"
%typemap(jtype) char *, char *&, char[ANY], char[]               "byte[]"
%typemap(jstype) char *, char *&, char[ANY], char[]               "byte[]"

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
%include "ImGuiColorTextEdit/TextEditor.h"
