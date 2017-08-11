package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by А on 10.03.2017.
 */
public class ConsoleHelper
{
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message)
    {
        System.out.println(message);
    }

    public static String readString()
    {
        String result = "";
        try
        {
            result = reader.readLine();
        }
        catch (IOException e)
        {
            ConsoleHelper.writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            return readString();
        }
        return result;
    }

    public static int readInt()
    {
        int result = 0;
        try {
            result = Integer.parseInt(readString());
        }
        catch (NumberFormatException e)
        {
            ConsoleHelper.writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            return readInt();
        }
        return result;
    }
}
