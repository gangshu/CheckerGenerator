package boya.research.abb.checkergenerator.utils;

import java.util.*;
import java.io.*;
class StreamGobbler extends Thread
{
    InputStream is;
    String type;
    
    StreamGobbler(InputStream is, String type)
    {
        this.is = is;
        this.type = type;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                System.out.println(type + ">" + line);    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}
public class ExecWrapper
{
    String cmd;
    String[] cmds;
    String[] env;
    File dir;
    Boolean simple;
    public ExecWrapper(String cmd)
    {
        this.cmd = cmd;
        this.simple = true;
        
    }
    public ExecWrapper(String[] cmds, String[]env, File dir)
    {
        this.cmds = cmds;
        this.env = env;
        this.dir = dir;
        this.simple = false;
    }
    public void run()
    {
        /*
        if (args.length < 1)
        {
            System.out.println("USAGE: java GoodWindowsExec <cmd>");
            System.exit(1);
        }
        */
        try
        {            
            Runtime rt = Runtime.getRuntime();
           // System.out.println("Execing " + cmd[0] + " " + cmd[1] 
            //                   + " " + cmd[2]);
            Process proc;
            if(simple)
                proc = rt.exec(cmd);
            else
                proc = rt.exec(cmds, env, dir);
            // any error message?
            StreamGobbler errorGobbler = new 
                StreamGobbler(proc.getErrorStream(), "ERROR");            
            
            // any output?
            StreamGobbler outputGobbler = new 
                StreamGobbler(proc.getInputStream(), "OUTPUT");
                
            // kick them off
            errorGobbler.start();
            outputGobbler.start();
                                    
            // any error???
            int exitVal = proc.waitFor();
            System.out.println("ExitValue: " + exitVal);        
        } catch (Throwable t)
          {
            t.printStackTrace();
          }
    }
}
