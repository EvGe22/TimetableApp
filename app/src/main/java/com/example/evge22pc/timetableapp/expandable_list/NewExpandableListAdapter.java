package com.example.evge22pc.timetableapp.expandable_list;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.evge22pc.timetableapp.R;
import com.example.evge22pc.timetableapp.data.UniversityClass;

public class NewExpandableListAdapter extends BaseExpandableListAdapter implements Serializable {

    static UniversityClass lastPressed;
    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<UniversityClass>> _listDataChild;

    public NewExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<UniversityClass>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final UniversityClass universityClass = (UniversityClass) getChild(groupPosition, childPosition);

        if (universityClass.isPressed()) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.new_list_item, null);


            TextView numText = (TextView) convertView.findViewById(R.id.numText);
            TextView classNumText = (TextView) convertView.findViewById(R.id.classNumText);
            TextView teacherText = (TextView) convertView.findViewById(R.id.teacherText);
            TextView homeworkText = (TextView) convertView.findViewById(R.id.homeworkText);
            TextView classTypeText = (TextView) convertView.findViewById(R.id.classTypeText);
            TextView nameText = (TextView) convertView.findViewById(R.id.nameText);

            numText.setText(Integer.toString(universityClass.getNum()));
            classNumText.setText(universityClass.getClass_num().replace("/","\n"));
            nameText.setText(universityClass.getName());
            classTypeText.setText(universityClass.getClass_type() == 1 ? "лекц." : "практ.");
            teacherText.setText(universityClass.getTeacher());
            homeworkText.setText(universityClass.getHomework());
        } else{
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.new_list_item_min, null);

            TextView numText = (TextView) convertView.findViewById(R.id.numText);
            TextView classNumText = (TextView) convertView.findViewById(R.id.classNumText);
            TextView classTypeText = (TextView) convertView.findViewById(R.id.classTypeText);
            TextView nameText = (TextView) convertView.findViewById(R.id.nameText);

            numText.setText(Integer.toString(universityClass.getNum()));
            classNumText.setText(universityClass.getClass_num().replace("/","\n"));
            nameText.setText(universityClass.getName());
            classTypeText.setText(universityClass.getClass_type() == 1 ? "лекц." : "практ.");
        }




        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (lastPressed!=null) lastPressed.setPressed(false);
                universityClass.setPressed(true);
                lastPressed = universityClass;
                notifyDataSetChanged();             //TODO check dis out
            }
        });

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (this._listDataChild.get(this._listDataHeader.get(groupPosition))!=null) ?
                this._listDataChild.get(this._listDataHeader.get(groupPosition))
                .size() : 0; //NOT NULL
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        if (this._listDataChild.get(this._listDataHeader.get(groupPosition))==null || this._listDataChild.get(this._listDataHeader.get(groupPosition)).size()==0){
            headerTitle = headerTitle + " пар нет";
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.list_header);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
