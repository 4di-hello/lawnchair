package app.lawnchair.gestures

/**
 * LC: A home screen view (app icon or folder) that may own swipe gestures. Pagers use this to
 * leave a touch to the view instead of scrolling when the view has a gesture for that axis.
 */
interface IconSwipeGestureTarget {
    fun hasConfiguredHorizontalIconSwipeGesture(): Boolean
    fun hasConfiguredVerticalIconSwipeGesture(): Boolean
}
