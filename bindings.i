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

%apply unsigned long long { size_t };
%apply const unsigned long long & { const size_t & };

%include "imgui/imgui.h"
%include "ImGuiColorTextEdit/TextEditor.h"
