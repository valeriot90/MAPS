package com.statbike.valtan.statbike;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ShowActivity extends Activity{

    private String LogNameFile[];
    private File file_list[];
    private int NumOfLogs=0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.show);

        String newFolder = "/StatApp";
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File dir = new File(extStorageDirectory + newFolder + File.separator);

        LogNameFile=dir.list();

        //Log.d("MSG: ", "LOGNAMEFILE " + LogNameFile);
        file_list=dir.listFiles();

        //Log.d("MSG: ", "file_list " + file_list);
        if(LogNameFile == null)
            Toast.makeText(getApplicationContext(), "Nessun log presente", Toast.LENGTH_LONG).show();

        else {
            NumOfLogs = LogNameFile.length;
            Log.d("MSG: ", "NumOfLog " + NumOfLogs);
            if (NumOfLogs == 0)
                Toast.makeText(getApplicationContext(), "Nessun log presente", Toast.LENGTH_LONG).show();
            else {



                Log.d("Files", "Size: " + file_list.length);
                String[] loglist = new String[NumOfLogs];

                for (int i = 0; i < NumOfLogs; i++) {
                    loglist[i] = file_list[i].getName();
                }

                Log.d("Files", "A");
                setContentView(R.layout.show);

                Log.d("Files", "B");


                Log.d("Files", "C");
                ListView listView = (ListView) findViewById(R.id.list_item);

                Log.d("Files", "D");
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.showview, R.id.textList, loglist);

                //for(int i =0; i<adapter.getCount(); i++)
                //    Log.d("MSG: ", "Log: " + loglist[i]);

                //listView.setOnClickListener();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adattatore, final View componente, int pos, long id){
                        // qui dentro stabilisco cosa fare dopo il click
                        Log.d("INDEX", "Gli passo questo indice: "+pos);
                        Intent intent = new Intent(
                                getApplicationContext(),
                                MapsActivity.class
                        );
                        //PASSO L'INDICE PER VEDERE QUALE FILE DEVO APRIRE
                        intent.putExtra("chiave",pos); //int

                        startActivity(intent);
                    }
                });

                if(listView != null ) {
                    //Log.d("MSG: ", "LISTVIEW OK, SETTO");
                    if(adapter == null) Log.d("MSG: ", "ADAPTER NULL");
                    listView.setAdapter(adapter);
                }
                else    Log.d("MSG: ", "LISTVIEW NULL");
            }
        }
    }

}
