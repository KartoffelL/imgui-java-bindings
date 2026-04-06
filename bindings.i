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

%apply (char* INOUT, size_t BUFFER_SIZE) { (char* buf, size_t buf_size) };

%include "imgui/imgui.h"
%include "ImGuiColorTextEdit/TextEditor.h"
