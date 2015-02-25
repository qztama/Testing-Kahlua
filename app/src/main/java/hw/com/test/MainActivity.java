package hw.com.test;

import android.content.res.AssetManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.LuaReturn;
import se.krka.kahlua.integration.annotations.LuaMethod;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.KahluaUtil;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;


public class MainActivity extends ActionBarActivity {

    private final KahluaConverterManager converterManager = new KahluaConverterManager();
    private final J2SEPlatform platform = new J2SEPlatform();
    private final KahluaTable env = platform.newEnvironment();
    private final KahluaThread thread = new KahluaThread(platform, env);
    private final LuaCaller caller = new LuaCaller(converterManager);
    private final LuaJavaClassExposer exposer = new LuaJavaClassExposer(converterManager, platform, env);
    private EditText text1;
    private TextView textout;
    private String script;
    private String test;

    /*public class setString implements JavaFunction {

        @Override
        public int call(LuaCallFrame callFrame, int nArguments) {
            Object arg = callFrame.get(0);

            if (arg == null || !(arg instanceof String)) {
                throw new IllegalArgumentException("Expected a string argument but got " + arg);
            }

            String s = (String) arg;
            setString(s);
            return 0;
        }
    }*/

    private static class StringTest {
        @LuaMethod(name = "getString")
        public String getString(){
            return "GetString worked.";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        exposer.exposeClass(StringTest.class);

        test = "NOT WORKING";


        //builds a string from the file in assets
        try{
            AssetManager am = getAssets();
            InputStream is = am.open("test.lua");

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String temp = "";
            StringBuilder sb = new StringBuilder();

            while((temp=br.readLine())!=null)
                sb.append(temp + "\n");

            script = sb.toString();

        }catch(IOException ep){ script = "";}

        try {
            LuaClosure closure = LuaCompiler.loadstring(script, "", env);
            //caller.protectedCall(thread, closure, new StringTest());
            caller.protectedCall(thread, closure, new StringTest());
        }catch(IOException ep){}

        test = (String) env.rawget("var");
        textout = (TextView) findViewById(R.id.textout);
        textout.setText(test);
    }

    /*@LuaMethod(global = true)
    public void setString(String s)
    {
        test = s;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
