%module imgui

%{
#include "imgui/imgui.h"
%}

%varargs(5, const char* arg = "") TextV;
%varargs(5, const char* arg = "") TextColoredV;
%varargs(5, const char* arg = "") TextDisabledV;
%varargs(5, const char* arg = "") TextWrappedV;
%varargs(5, const char* arg = "") LabelTextV;
%varargs(5, const char* arg = "") BulletTextV;

%varargs(5, const char* arg = "") TreeNodeV_1_1SWIG_10;
%varargs(5, const char* arg = "") TreeNodeV_1_1SWIG_11;
%varargs(5, const char* arg = "") TreeNodeExV_1_1SWIG_10;
%varargs(5, const char* arg = "") TreeNodeExV_1_1SWIG_11;

%varargs(5, const char* arg = "") SetTooltipV;
%varargs(5, const char* arg = "") SetItemTooltipV;
%varargs(5, const char* arg = "") LogTextV;
%varargs(5, const char* arg = "") DebugLogV;
%varargs(5, const char* arg = "") ImGuiTextBuffer_1appendfv;

%include "imgui/imgui.h"
