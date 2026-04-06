%module imgui

%{
#include "imgui/imgui.h"
#include "ImGuiColorTextEdit/TextEditor.h"
%}

%include "enumtypeunsafe.swg"
%include "std_vector.i"
%include "various.i"

%javaconst(1);

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
