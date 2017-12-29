package com.qthekan.qhere.joystick;


public interface JoystickListener {
    void onDown();

    /**
     *
     * @param x : longitude move distance ㅡ
     * @param y : latitude move distance ㅣ
     * @param offset : move power
     */
    void onDrag(float x, float y, float offset);

    void onUp();
}
