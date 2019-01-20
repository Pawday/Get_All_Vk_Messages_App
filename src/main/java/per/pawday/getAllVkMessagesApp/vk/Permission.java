package per.pawday.getAllVkMessagesApp.vk;


import org.json.simple.JSONObject;
import java.util.ArrayList;


public class Permission
{
    private JSONObject check(long mask, String tokenType)
    {
        JSONObject reter = new JSONObject();


        String[] userPermissionsStr =
                {
                        "notify",
                        "friends",
                        "photos",
                        "audio",
                        "video",
                        "stories",
                        "pages",
                        "status",
                        "notes",
                        "messages",
                        "wall",
                        "ads",
                        "offline",
                        "docs",
                        "groups",
                        "notifications",
                        "stats",
                        "email",
                        "market"
                };

        long[] userPermissionsLong =
                {
                        1,
                        2,
                        4,
                        8,
                        16,
                        64,
                        128,
                        1024,
                        4096,
                        8192,
                        32768,
                        65536,
                        131072,
                        262144,
                        524288,
                        1048576,
                        4194304,
                        134217728
                };

        String[] groupPermissionsStr =
                {
                        "stories",
                        "photos",
                        "app_widget",
                        "messages",
                        "docs",
                        "manage"
                };

        long[] groupPermissionsLong =
                {
                        1,
                        4,
                        64,
                        4096,
                        131072,
                        262144
                };




        if (tokenType.equals("User"))
        {
            // user permissions



            for (int i = 0; i < userPermissionsLong.length; i++)
            {
                if ((mask & userPermissionsLong[i]) == userPermissionsLong[i])
                {
                    reter.put(userPermissionsStr[i],true);
                }
                else
                {
                    reter.put(userPermissionsStr[i],false);
                }
            }
        }
        else if (tokenType.equals("Group"))
        {
            for (int i2 = 0; i2 < groupPermissionsLong.length; i2++)
            {
                if ((mask & groupPermissionsLong[i2]) == groupPermissionsLong[i2])
                {
                    reter.put(groupPermissionsStr[i2],true);
                }
                else
                {
                    reter.put(groupPermissionsStr[i2],false);
                }
            }

        }

        return reter;
    }
    public boolean checkPermission(String token, String tokenType)
    {
        Request vkReq = new Request();

        boolean reter = false;





        String[] requiredGroupPermissions = {"messages"};
        String[] requiredUserPermissions = {"messages"};



        if (tokenType.equals("Group"))
        {
            JSONObject res = vkReq.post(token,"groups.getTokenPermissions",new JSONObject());
            res = (JSONObject) res.get("response");

            long mask = (long) res.get("mask");


            JSONObject pers = this.check(mask,tokenType);
            ArrayList requiredGroupPermissionsArr = new ArrayList();

            for (int i = 0; i < requiredGroupPermissions.length; i++)
            {
                if ((boolean) pers.get(requiredGroupPermissions[i]))
                {
                    requiredGroupPermissionsArr.add(true);
                }
                else
                {
                    requiredGroupPermissionsArr.add(false);
                }
            }
            boolean er = true;

            for (int i = 0; i < requiredGroupPermissionsArr.size(); i++)
            {
                er = er && (boolean) requiredGroupPermissionsArr.get(i);
            }

            if (er)
            {
                reter = true;
            }
        }

        if (tokenType.equals("User"))
        {
            JSONObject res = vkReq.post(token,"account.getAppPermissions",new JSONObject());

            long mask = (long) res.get("response");



            JSONObject pers = this.check(mask,tokenType);

            ArrayList requiredUserPermissionsArr = new ArrayList();

            for (int i = 0; i < requiredUserPermissions.length; i++)
            {
                if ((boolean) pers.get(requiredGroupPermissions[i]))
                {
                    requiredUserPermissionsArr.add(true);
                }
                else
                {
                    requiredUserPermissionsArr.add(false);
                }
            }
            boolean mar = true;

            for (int i = 0; i < requiredUserPermissionsArr.size(); i++)
            {
                mar = mar && (boolean) requiredUserPermissionsArr.get(i);
            }

            if (mar)
            {
                reter = true;
            }
        }


        return reter;

    }
}
