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
%ignore TreeNodeV_1_1SWIG_10;
%ignore TreeNodeV_1_1SWIG_11;
%ignore TreeNodeExV_1_1SWIG_10;
%ignore TreeNodeExV_1_1SWIG_11;
%ignore SetTooltipV;
%ignore SetItemTooltipV;
%ignore LogTextV;
%ignore DebugLogV;
%ignore ImGuiTextBuffer_1appendfv;

%include "imgui/imgui.h"
