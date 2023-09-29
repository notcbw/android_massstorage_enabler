package com.notcbw.usbgadgetconfig;

import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class USBGadgetController {

    private String gadgetId;
    private String configId;
    private String defaultUDC;
    private HashMap<String, String> gadgetMap;

    public USBGadgetController() {
        Shell s = Shell.getShell();
        if (!(s.isAlive() && s.isRoot()))
            throw new RuntimeException("Failed to get root shell!");

        Shell.Result res;
        res = Shell.cmd("ls /config/usb_gadget").exec();
        if (res.isSuccess()) {
            gadgetId = res.getOut().get(0);
        } else {
            throw new RuntimeException("No ConfigFS folder for USB gadget!");
        }

        res = Shell.cmd(String.format("ls /config/usb_gadget/%s/configs", gadgetId)).exec();
        if (res.isSuccess()) {
            configId = res.getOut().get(0);
        } else {
            configId = "c.1";
            Shell.cmd(String.format("mkdir /config/usb_gadget/%s/configs/c.1", gadgetId));
        }

        res = Shell.cmd(String.format("cat /config/usb_gadget/%s/UDC", gadgetId)).exec();
        if (res.isSuccess()) {
            defaultUDC = res.getOut().get(0);
        } else {
            throw new RuntimeException("Cannot get default UDC device");
        }

        gadgetMap = new HashMap<>();
    }

    public List<String> getAvailableFunctions() {
        Shell.Result res;
        res = Shell.cmd(String.format("ls /config/usb_gadget/%s/functions", gadgetId)).exec();
        if (res.isSuccess()) {
            return res.getOut();
        } else {
            return null;
        }
    }

    public boolean addFunction(String func) {
        Shell.Result res;
        res = Shell.cmd(String.format("mkdir /config/usb_gadget/%s/functions/%s", gadgetId, func)).exec();
        return res.isSuccess();
    }

    public String enableFunction(String func) {
        String key = String.format("f%d", gadgetMap.size() + 1);
        Shell.Result res;
        res = Shell.cmd(String.format("mkdir /config/usb_gadget/%s/configs/%s/%s",
                gadgetId, configId, key)).exec();
        if (res.isSuccess()) {
            Shell.Result res2 = Shell.cmd(String.format("ln -s /config/usb_gadget/%s/functions/%s " +
                    "/config/usb_gadget/%s/config/%s/%s",
                    gadgetId, func, gadgetId, configId, key)).exec();
            if (res.isSuccess()) {
                gadgetMap.put(key, func);
                return key;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean disableFunction(String key) {
        Shell.Result res;
        res = Shell.cmd(String.format("rm -r /config/usb_gadget/%s/configs/%s/%s",
                gadgetId, configId, key)).exec();
        if (res.isSuccess()) {
            gadgetMap.remove(key);
            return true;
        } else {
            return false;
        }
    }

    public boolean setMassStorageFile(String func, int index, String file) {
        if (index < 0) return false;
        Shell.Result res;
        res = Shell.cmd(String.format("ls /config/usb_gadget/%s/functions/%s", gadgetId, func)).exec();
        if (res.isSuccess()) {
            // check if the lun with specified index exist
            String s = "";
            String indexStr = String.format("lun.%d", index);
            for (String s2: res.getOut()) {
                if (s2.equals(indexStr)) {
                    s = s2;
                    break;
                }
            }

            // if it does not exist, create lun with the specified index
            if (s.isEmpty()) {
                s = indexStr;
                res = Shell.cmd(String.format("ls /config/usb_gadget/%s/functions/%s/%s",
                        gadgetId, func, s)).exec();
                if (!res.isSuccess()) return false;
            }

            // set file
            res = Shell.cmd(String.format("echo %s > /config/usb_gadget/%s/functions/%s/%s/file",
                    file, gadgetId, func, s)).exec();
            return res.isSuccess();

        } else {
            return false;
        }
    }

    public void disableGadget() {
        Shell.Result res = Shell.cmd(String.format("echo \"\" > /config/usb_gadget/%s/UDC", gadgetId)).exec();
        if (!res.isSuccess())
            throw new RuntimeException(res.getOut().get(0));
    }

    public void enableGadget() {
        Shell.Result res = Shell.cmd(String.format("echo %s > /config/usb_gadget/%s/UDC", defaultUDC, gadgetId)).exec();
        if (!res.isSuccess())
            throw new RuntimeException(res.getOut().get(0));
    }
}
