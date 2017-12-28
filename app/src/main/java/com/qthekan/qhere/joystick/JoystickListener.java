package com.qthekan.qhere.joystick;


public interface JoystickListener {
    void onDown();

    /**
     * @param degrees -180 -> 180.
     * @param offset  normalized, 0 -> 1.
     */
    //void onDrag(float degrees, float offset);
    void onDrag(float x, float y, float offset);

    void onUp();
}
