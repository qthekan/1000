package com.qthekan.qhere.raid;

public class Team
{
    public static final int NONE = 0;
    public static final int BLUE = 1;
    public static final int RED = 2;
    public static final int YELLOW = 3;


    public static String getTeamStr(int i)
    {
        switch (i)
        {
            case 1:
                return "Blue";
            case 2:
                return "Red";
            case 3:
                return "Yellow";
            default:
                return "Unknown";
        }
    }
}
