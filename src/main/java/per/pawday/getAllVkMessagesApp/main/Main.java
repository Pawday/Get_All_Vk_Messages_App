package per.pawday.getAllVkMessagesApp.main;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import per.pawday.getAllVkMessagesApp.json.Formatter;
import per.pawday.getAllVkMessagesApp.vk.Request;


public class Main
{
    public static void main(String[] args)
    {
        Formatter formatter = new Formatter();
        JSONParser parser = new JSONParser();
        Request vkReq = new Request();

        File tokenFile = new File("token.json");

        JSONObject tokenObj = new JSONObject();

        tokenObj.put("token","");


        String tokenType;   // "User" , "Group"



        String token = "";




        if ( ! tokenFile.exists())
        {
            try
            {
                tokenFile.createNewFile();

                Writer writer = new FileWriter(tokenFile);

                writer.write(formatter.formatJSONStr(tokenObj.toString(),5));

                writer.close();

                System.out.println("Введите токен в файл token.json");
                System.exit(0);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }


        try
        {
            tokenObj = (JSONObject) parser.parse(new FileReader(tokenFile));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        if (tokenObj.get("token").equals(""))
        {
            System.out.println("Введите токен в файл token.json");
            System.exit(0);
        }
        else
        {
            token = (String) tokenObj.get("token");
        }


        {
            JSONObject res = vkReq.post(token,"users.get",new JSONObject());

            //confirmation part
            {
                if (res.get("error") != null)
                {
                    JSONObject err = (JSONObject) res.get("error");
                    if ((long) err.get("error_code") == 5 )
                    {
                        System.out.println("Токен не действителен!");
                        System.exit(-1);
                    }
                }
            }

            {
                JSONArray arr = (JSONArray) res.get("response");
                if (arr.size() == 0)
                {
                    tokenType = "Group";
                }
                else
                {
                    tokenType = "User";
                }
            }

            // confirmation permissions part
            {
                per.pawday.getAllVkMessagesApp.vk.Permission Permissions = new per.pawday.getAllVkMessagesApp.vk.Permission();

                boolean permission = Permissions.checkPermission(token.toString(),tokenType);

                if (!permission)
                {
                    System.out.println("У текущего токена не достаточно прав!");
                    System.exit(-1);
                }
            }
        }



        JSONArray conversations = new JSONArray();

        {
            JSONObject params = new JSONObject();
            params.put("count",0);
            JSONObject resp = vkReq.post(token,"messages.getConversations",params);
            resp = (JSONObject) resp.get("response");
            long countConversations = (long) resp.get("count");
            long countExecuteSubquerys = (long) Math.ceil(countConversations / (double)200);
            long countExecuteQuerys = (long) Math.ceil(countExecuteSubquerys / (double)25);

            long lastExecuteSubqueryConversationsCount = countConversations - 200 * (countExecuteSubquerys - 1);
            long lastExecuteSubquerysCount = countExecuteSubquerys - (countExecuteQuerys - 1) * 25;


            StringBuilder vkScript = new StringBuilder();

            for (int i = 0; i < countExecuteQuerys; i++)
            {
                vkScript.delete(0,vkScript.length());
                vkScript.append("return [");

                int c = 25;

                if (i == countExecuteQuerys - 1)
                {
                    c = (int) lastExecuteSubquerysCount;
                }

                for (int i2 = 0; i2 < c; i2++ )
                {

                    int c2 = 200;
                    if (i2 == (c - 1) && i == countExecuteQuerys - 1)
                    {
                        c2 = (int) lastExecuteSubqueryConversationsCount;
                    }

                    vkScript.append("API.messages.getConversations({\"count\":" + c2 + ",\"offset\":" + ((i * 5000) + (i2 * 200) ) + "})");

                    if (i2 == (c - 1))
                    {
                        vkScript.append("];");
                    }
                    else
                    {
                        vkScript.append(",");
                    }

                }


                params.clear();


                params.put("code",vkScript.toString());

                JSONObject response = vkReq.post(token,"execute",params);
                JSONArray miniConvArrPart5000 = (JSONArray) response.get("response");

                for(int i2 = 0; i2 < miniConvArrPart5000.size(); i2++)
                {
                    JSONObject convObjPart200 = (JSONObject) miniConvArrPart5000.get(i2);
                    JSONArray items = (JSONArray) convObjPart200.get("items");
                    for (int i3 = 0; i3 < items.size(); i3++)
                    {
                        conversations.add(items.get(i3));
                    }
                }
            }
        }


        JSONObject main = new JSONObject();

        {
            JSONObject meta = new JSONObject();

            if (tokenType.equals("User"))
            {
                meta.put("Type","User");

                JSONObject user = new JSONObject();

                JSONObject res = vkReq.post(token,"users.get", new JSONObject());
                JSONArray arr = (JSONArray) res.get("response");

                res = (JSONObject) arr.get(0);


                user.put("id",res.get("id"));
                user.put("name",res.get("first_name"));
                user.put("surname",res.get("last_name"));

                meta.put("UserInfo",user);
                meta.put("Time", new Date().getTime());

            } else
            {
                meta.put("Type","Group");
                meta.put("Time", new Date().getTime());
            }

            main.put("Meta",meta);

        }

        JSONArray body = new JSONArray();



        for (int i = 0; i < conversations.size(); i++)
        {



            System.out.println("Get conversations: " + (i + 1) + " of " + conversations.size() + ".");
            JSONObject conversation = new JSONObject();


            JSONObject conversationInfo = (JSONObject) conversations.get(i);
            conversationInfo = (JSONObject) conversationInfo.get("conversation");

            conversationInfo.remove("last_message");
            conversation.put("conversation",conversationInfo);

            JSONObject peer = (JSONObject) conversationInfo.get("peer");

            JSONObject params = new JSONObject();

            params.put("count",0);
            params.put("peer_id",peer.get("id"));

            JSONObject resp = vkReq.post(token,"messages.getHistory",params);

            params.clear();

            resp = (JSONObject) resp.get("response");




            long countMessages = (long) resp.get("count");
            long countExecuteSubquerys = (long) Math.ceil(countMessages / (double)200);
            long countExecuteQuerys = (long) Math.ceil(countExecuteSubquerys / (double)25);


            long lastExecuteSubquerysCount = countExecuteSubquerys - (countExecuteQuerys - 1) * 25;
            long lastExecuteSubqueryConversationsCount = countMessages - 200 * (countExecuteSubquerys - 1);



            JSONArray messagesArr = new JSONArray();

            StringBuilder vkScript = new StringBuilder();

            for (int i2 = 0; i2 < countExecuteQuerys; i2++)
            {

                vkScript.delete(0,vkScript.length());
                vkScript.append("return[");

                int c = 25;

                if (i2 == countExecuteQuerys - 1)
                {
                    c = (int) lastExecuteSubquerysCount;
                }

                for (int i3 = 0; i3 < c; i3++)
                {
                    int c2 = 200;
                    if (i3 == (c - 1) && i2 == countExecuteQuerys - 1)
                    {
                        c2 = (int) lastExecuteSubqueryConversationsCount;
                    }

                    vkScript.append("API.messages.getHistory({\"count\":" + c2 + ",\"offset\":" + ((i2 * 5000) + (i3 * 200) ) + ",\"peer_id\":" + peer.get("id") + "})");

                    if (i3 == (c - 1))
                    {
                        vkScript.append("];");
                    }
                    else
                    {
                        vkScript.append(",");
                    }
                }

                JSONObject parame = new JSONObject();

                parame.put("code",vkScript.toString());

                JSONObject vkRes = vkReq.post(token,"execute",parame);
                JSONArray messagesArrPart200 = (JSONArray) vkRes.get("response");



                for (int i3 = 0; i3 < messagesArrPart200.size();i3++)
                {
                    JSONObject messObj = (JSONObject) messagesArrPart200.get(i3);
                    JSONArray items = (JSONArray) messObj.get("items");

                    for (int i4 = 0; i4 < items.size(); i4++)
                    {
                        messagesArr.add(items.get(i4));
                    }
                }

            }

            conversation.put("messages",messagesArr);



            body.add(conversation);



        }





        main.put("Body",body);


        char[] mainJSON = formatter.formatJSONStr(main.toString(),5).toCharArray();


        try
        {
            File file = new File("Result.json");

            if (file.exists())
            {
                file.delete();
            }
            OutputStreamWriter fileWhitter = new OutputStreamWriter(new FileOutputStream(file,true),StandardCharsets.UTF_8);

            for (int i = 0; i < (int) Math.ceil(mainJSON.length / (double)10000); i++)
            {
                System.out.println("Write to file: " + (i + 1) + " of " + ((int) Math.ceil(mainJSON.length / (double)10000)) + ".");

                int coount = 10000;

                if (i == ((int) Math.ceil(mainJSON.length / (double)10000) - 1))
                {
                    System.out.println("End");
                    coount = mainJSON.length - i * 10000;
                }



                fileWhitter.write(mainJSON,i * 10000,coount);
                fileWhitter.flush();


            }

            try
            {
                fileWhitter.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
