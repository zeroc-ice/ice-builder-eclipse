//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

package com.zeroc.icebuilderplugin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamReader extends Thread
{
    public StreamReader(InputStream in, StringBuffer out)
    {
        _in = new BufferedReader(new InputStreamReader(in), 1024);
        _out = out;
    }

    public void run()
    {
        try
        {
            char[] buf = new char[1024];
            while(true)
            {
                int read = _in.read(buf);
                if(read == -1)
                {
                    break;
                }
                _out.append(buf, 0, read);
            }
        }
        catch(Exception e)
        {
        }
        finally
        {
            try
            {
                _in.close();
            }
            catch(IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    private StringBuffer _out;
    private BufferedReader _in;
}
