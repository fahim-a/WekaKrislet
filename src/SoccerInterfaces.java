//
//	File:			SoccerInterfaces.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//

//      Modified by:     Edgar Acosta
//      Date:            March 5, 2008
//      Added the bye command



//***************************************************************************
//
//	This interface declares functions which are used to send
//	command to player
//
//***************************************************************************
interface SendCommand
{
    // This function sends move command to the server
    void move(double x, double y);
    // This function sends turn command to the server
    void turn(double moment);
    void turn_neck(double moment);
    // This function sends dash command to the server
    void dash(double power);
    // This function sends kick command to the server
    void kick(double power, double direction);
    // This function sends say command to the server
    void say(String message);
    // This function sends chage_view command to the server
    void changeView(String angle, String quality);
    // This function sends a bye command to the server
    void bye();
}




interface SensorInput
{
    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info);

    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message);

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message);
}
