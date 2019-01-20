package per.pawday.getAllVkMessagesApp.vk;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Request
{
    public JSONObject post(String token, String method, JSONObject params)
    {


        try
        {
            Thread.sleep(334);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }


        JSONParser parser = new JSONParser();

        URL url = null;
        try
        {
            url = new URL("https://api.vk.com/method/" + method);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }


        params.put("access_token",token);
        params.put("v","5.92");


        Object[] keys = params.keySet().toArray();



        StringBuilder strParams = new StringBuilder();

        for (int i = 0; i < keys.length; i++)
        {
            strParams.append(keys[i] + "=" + params.get(keys[i]));

            if (i < keys.length - 1)
            {
                strParams.append("&");
            }
        }

        HttpURLConnection connection = null;
        JSONObject returning = null;

        try
        {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(strParams.toString().getBytes());
            outputStream.flush();
            outputStream.close();


            InputStream inputStream = connection.getInputStream();

            returning = (JSONObject) parser.parse(new InputStreamReader(inputStream));




        }
        catch (IOException e)
        {
            System.out.println("Проблемма с подключением к VK");
            System.exit(-1);
        }
        catch (ParseException e)
        {}

        if (returning.get("error") != null)
        {
            JSONObject error = (JSONObject) returning.get("error");

            System.out.println("VK_Error: " + error.get("error_msg"));


        }

        return returning;

    }
}
