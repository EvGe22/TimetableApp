package com.example.evge22pc.timetableapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.evge22pc.timetableapp.data.DBHelper;
import com.example.evge22pc.timetableapp.data.MyLog;
import com.example.evge22pc.timetableapp.data.UniversityClass;
import com.example.evge22pc.timetableapp.expandable_list.ExpandableListAdapter;
import com.example.evge22pc.timetableapp.expandable_list.NewExpandableListAdapter;
import com.example.evge22pc.timetableapp.httpconnection.DownloadTask;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;




public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private enum TypeOfTask {CLASSES};
    private enum OperationResult {SUCCESS, FAILURE, ERROR, CANCELLED, EMPTY_RESPONSE, WTF}

    public static final String PREF_NAME = "TTSettings";

    Switch mySwitch;
    TextView weekType;
    DBHelper dbHelper;
    RadioButton weekIndicator;
    Drawer drawer;

    SwipeRefreshLayout swipeRefreshLayout;
    NewExpandableListAdapter newListAdapter;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    HashMap<String, List<UniversityClass>> newListDataChild;

    boolean isWeekEven;
    public static boolean showFullTeacherName, showHomework;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


         drawer = new DrawerBuilder().withActivity(this).withHeader(R.layout.drawer_header).addDrawerItems(
                new PrimaryDrawerItem().withName(R.string.drawer_item_home).withBadge("99").withIdentifier(1),
                new PrimaryDrawerItem().withName(R.string.drawer_item_current),
                new PrimaryDrawerItem().withName(R.string.drawer_item_custom).withBadge("6").withIdentifier(2),
                new SectionDrawerItem().withName(R.string.drawer_item_settings),
                new SecondaryDrawerItem().withName(R.string.drawer_item_help),
                new SecondaryDrawerItem().withName(R.string.drawer_item_open_source),
                new DividerDrawerItem(),
                new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withBadge("12+").withIdentifier(1)
        ).build();

        //ImageView headerBG = (ImageView) findViewById(R.id.header_bg);
        dbHelper = new DBHelper(this);
        mySwitch = (Switch) findViewById(R.id.switch1);
        expListView = (ExpandableListView) findViewById(R.id.expListView);
        weekType = (TextView) findViewById(R.id.weekType);
        weekIndicator = (RadioButton) findViewById(R.id.radioButton);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        //headerBG.setImageResource(R.drawable.header);
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ww");
        int intFlag = Integer.parseInt(simpleDateFormat.format(date));
        MyLog.v("WEEK FLAG IS"+Integer.toString(intFlag));
        isWeekEven = intFlag % 2 == 0;


        /*
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        showFullTeacherName = sharedPreferences.getBoolean("showTeacherFullName",true);
        showHomework = sharedPreferences.getBoolean("showHomework",true);
        */

        weekType.setText(isWeekEven ? "Парная неделя":"Непарная неделя");
        mySwitch.setChecked(isWeekEven);
        weekIndicator.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new SwitchListener());

        swipeRefreshLayout.setColorSchemeResources(R.color.material_deep_purple_A200);
        swipeRefreshLayout.setOnRefreshListener(this);

        updateUI();
    }
/*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("elAdapter", newListAdapter);
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("showTeacherFullName",showFullTeacherName);
        editor.putBoolean("showHomework",showHomework);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        expListView.setAdapter((NewExpandableListAdapter) savedInstanceState.getSerializable("elAdapter"));
    }
    */
    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        String flag;
        flag = !isWeekEven ? "<= 1" : ">= 1";

        listDataHeader.add("Понедельник");
        listDataHeader.add("Вторник");
        listDataHeader.add("Среда");
        listDataHeader.add("Четверг");
        listDataHeader.add("Пятница");

        for (int i = 1; i <= listDataHeader.size(); i++) {
            List<String> list = new ArrayList<String>();
            DBHelper.ClassesCursor cursor;
            cursor = dbHelper.getClasses(i,flag);
            if (cursor.getCount()<=0){
                list.add("Выходной. Пар нет");
            } else {
                cursor.moveToFirst();
                do {
                    list.add(cursor.toString());
                } while (cursor.moveToNext());
            }
            cursor.close();
            listDataChild.put(listDataHeader.get(i - 1), list);
        }
    }

    private void newPrepareListData(){
        listDataHeader = new ArrayList<String>();
        newListDataChild = new HashMap<String, List<UniversityClass>>();

        String flag;
        flag = !isWeekEven ? "<= 1" : ">= 1";
        //TODO make new columns in table and cursor

        listDataHeader.add("Понедельник");
        listDataHeader.add("Вторник");
        listDataHeader.add("Среда");
        listDataHeader.add("Четверг");
        listDataHeader.add("Пятница");

        for (int i = 1; i <= listDataHeader.size(); i++) {
            List<UniversityClass> list = new ArrayList<UniversityClass>();
            DBHelper.ClassesCursor cursor;
            cursor = dbHelper.getClasses(i,flag);
            if (cursor.getCount()<=0){
                break;
            } else {
                cursor.moveToFirst();
                do {
                    UniversityClass universityClass = new UniversityClass(cursor.getId(),
                            cursor.getIntId(),cursor.getWeekDay(),cursor.getWeek(),
                            cursor.getNum(),cursor.getClassType(),cursor.getClassNum(),
                            cursor.getName(),cursor.getTeacher(),cursor.getHomework());
                    list.add(universityClass);
                } while (cursor.moveToNext());
            }
            cursor.close();
            newListDataChild.put(listDataHeader.get(i - 1), list);
        }
    }


    /**
     * Displays the UI on the first launch
     */
    private void displayUI() {

    }

    /**
     * Updates the UI after some changes are made to the DB/elements
     */
    private void updateUI() {
        newPrepareListData();

        newListAdapter = new NewExpandableListAdapter(this, listDataHeader, newListDataChild);

        // setting list adapter
        expListView.setAdapter(newListAdapter);
        int dayNum = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        int num;
        if (dayNum==1 || dayNum==7) num = 0;
        else num=dayNum-2;
        expListView.expandGroup(num);
        expListView.invalidate();
    }

    /**
     * Downloads the new data to DB
     */
    private void updateDB(){
        (new UpdateDB()).execute(TypeOfTask.CLASSES);
    }

    private void showToast(String s){
        Toast.makeText(this,s,Toast.LENGTH_LONG).show();
    }




    /**
     * Class that handles the work with the external DB
     */
    class UpdateDB extends AsyncTask<TypeOfTask, Void, OperationResult> {

        @Override
        protected void onPreExecute() {
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                cancel(true);
                updateUI();
                showToast("Could not connect to the network.\n Check your internet connection");
            }
        }

        @Override
        protected OperationResult doInBackground(TypeOfTask... type) {
            if (isCancelled()) return OperationResult.CANCELLED; //this will never happen
            switch (type[0]){

                //This will download the new classes from the external DB
                case CLASSES: {
                    return updateClasses();
                }

                default:{
                    return OperationResult.WTF;
                }
            }

        }

        @Override
        protected void onPostExecute(OperationResult result) {
            swipeRefreshLayout.setRefreshing(false);
            switch (result){
                case SUCCESS:{
                    updateUI();
                    SharedPreferences settings = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    editor.putString("date",simpleDateFormat.format(new Date(System.currentTimeMillis())));
                    editor.apply();
                    showToast("Successfully updated the DB");
                    break;
                }

                case FAILURE:{
                    showToast("There was an error while downloading");
                    break;
                }

                case ERROR:{
                    showToast("An Exception occurred while processing\nJSON data");
                    break;
                }

                case CANCELLED:{
                    //well, this will never happen as well coz it won't even get to onPostExecute if I cancel the task, won't it?
                    showToast("Could not connect to the network.\n Check your internet connection");
                    break;
                }
                case EMPTY_RESPONSE:{
                    showToast("Empty response");
                    //updateUI();
                    break;
                }
                //This should really never-never happen, but who knows D:
                case WTF:{
                    showToast("How did you make this happen?!");
                    break;
                }
            }
        }

        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        private JSONObject readJsonFromStream(InputStream is) throws IOException, JSONException {
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                MyLog.v(jsonText.substring(1,40));
                return new JSONObject(jsonText);
            } finally {
                MyLog.v("Reading JSON");
                is.close();
            }
        }

        private OperationResult updateClasses(){
            MyLog.v("Updating the classes");
            try{
                SharedPreferences settings = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
                String group = settings.getString("group", "KC_31");
                String date = settings.getString("date","1970-01-01");
                JSONObject object = readJsonFromStream(DownloadTask.getStream(
                        String.format(
                        "http://i975021z.bget.ru/get_all_classes.php?group=%s&date=%s",
                                group, date)));
                switch (object.getInt("response")) {
                    case 2: {
                        JSONArray array = object.getJSONArray("classes");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int tmp = dbHelper.contains(obj.getString("id"));

                            boolean isDeleted = obj.getString("subject_name").equals("DELETED");
                            if (tmp>=0) {
                                if (isDeleted){
                                    dbHelper.deleteClass(tmp);
                                } else {
                                    dbHelper.updateClass("" + tmp, obj);
                                    MyLog.v("Updating for id = " + tmp);
                                }
                            }
                            else if (!isDeleted) dbHelper.addClass(obj);
                        }
                        break;
                    }

                    case 1:{
                        return OperationResult.EMPTY_RESPONSE;
                    }

                    case 0:{
                        return OperationResult.FAILURE;
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
                return OperationResult.ERROR;
            } catch (JSONException e) {
                e.printStackTrace();
                return OperationResult.ERROR;
            } catch (NullPointerException e){
                e.printStackTrace();
                return OperationResult.ERROR;
            }
            return OperationResult.SUCCESS;
        }




    }

    private class SwitchListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            isWeekEven = !isWeekEven;
            weekType.setText(isWeekEven ? "Парная неделя":"Непарная неделя");
            weekIndicator.setChecked(!weekIndicator.isChecked());
            updateUI();
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        updateDB();

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()){
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
