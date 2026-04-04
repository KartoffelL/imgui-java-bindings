%module imgui

%{
#include "imgui/imgui.h"
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
%ignore ImGuiTextBuffer_1appendfv;

%include "imgui/imgui.h"
