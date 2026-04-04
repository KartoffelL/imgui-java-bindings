%module imgui

%{
#include "imgui/imgui.h"
%}


%ignore *V;
%ignore *v;

%include "imgui/imgui.h"
