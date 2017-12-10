package com.af.pxl.analytics;

/**
 * Created by Aefyr on 10.12.2017.
 */

public class FirebaseConstants {
    class Generic{
        static final String GENERIC_ARG_ENABLED = "enabled";
    }

    class Tools{
        static final String EVENT_TOOL_PICKED = "tool_picked";
        static final String EVENT_TOOL_PICKED_ARG_TOOL = "tool";
        static final String TOOL_PENCIL = "pencil";
        static final String TOOL_ERASER = "eraser";
        static final String TOOL_FILL = "fill";
        static final String TOOL_MULTISHAPE = "multishape";
        static final String TOOL_COLOR_PICKER = "color_picker";
        static final String TOOL_COLOR_SWAPPER = "color_swapper";
        static final String TOOL_SELECTOR = "selector";
    }

    public class Canvas{
        static final String EVENT_SYMMETRY_CHANGED = "symmetry_changed";
        static final String EVENT_SYMMETRY_CHANGED_ARG_MODE = "mode";
        static final String MODE_HORIZONTAL = "horizontal";
        static final String MODE_VERTICAL = "vertical";

        static final String EVENT_CURSOR_MODE_CHANGED = "cursor_mode_toggled";

        static final String EVENT_GRID_VISIBILITY_CHANGED = "grid_visibility_changed";

        static final String EVENT_CANVAS_SPECIAL_ACTION = "canvas_action";
        static final String EVENT_CANVAS_SPECIAL_ACTION_ARG_ACTION = "action";
        public static final String ACTION_CLEAR = "clear";
        public static final String ACTION_MOVE = "move";
        public static final String ACTION_OVERLAY = "overlay";
        public static final String ACTION_CENTER = "center";
        public static final String ACTION_MIRROR = "mirror";
        public static final String ACTION_SAVE = "save";
    }

    public class Projects {
        static final String EVENT_PROJECT_OPENED = "project_opened";

        static final String EVENT_PROJECT_CREATED = "project_created";
        static final String EVENT_PROJECT_CREATED_ARG_VIA = "via";
        public static final String VIA_NORMAL = "normally";
        public static final String VIA_IMPORTING = "importing";
        public static final String VIA_DUPLICATING = "duplicating";

        static final String EVENT_PROJECT_EXPORTED = "project_exported";
        static final String EVENT_PROJECT_EXPORTED_ARG_SHARED = "shared";
    }
}
