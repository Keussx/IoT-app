package com.ksopha.thanetearth.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.ksopha.thanetearth.ormObject.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ksopha.thanetearth.R;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import de.codecrafters.tableview.SortableTableView;
import de.codecrafters.tableview.TableDataAdapter;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import io.realm.Realm;
import io.realm.Sort;


/**
 * Fragment for displaying history of alerts triggered
 * Created by Kelvin Sopha on 22/03/18.
 */
public class AlertsFragment extends Fragment {

    private SortableTableView tableView;
    private String[] headers = {"Date", "Message"};
    private final static SimpleDateFormat dateFormatter= new SimpleDateFormat("dd/MM/yy--hh:mm a ");
    private Realm realm;


    /**
     * called at creation of fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // prevent fragment from being re-created when parent Activity is destroyed
        setRetainInstance(true);
    }



    /**
     * called to instantiate fragment ui
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        realm = Realm.getDefaultInstance();

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_alerts, container, false);

        tableView = (SortableTableView)  root.findViewById(R.id.table_alerts);

        TableColumnWeightModel columnModel = new TableColumnWeightModel(2);
        columnModel.setColumnWeight(1, 2);
        columnModel.setColumnWeight(1, 4);
        tableView.setColumnModel(columnModel);

        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(getActivity(), headers));
        tableView.setColumnComparator(0, new LogComparator());

        return root;
    }



    /**
     * refresh the logs being shown in fragment
     */
    public void refreshLogViews(){
        // since latest ids
        List<Log> logs = realm.where(Log.class).findAll().sort("date", Sort.DESCENDING);

        android.util.Log.e("R",logs.size() + " items" );


        tableView.setDataAdapter(new LogTableDataAdapter(getActivity(), logs));
    }



    /**
     * Class used as table adapter
     */
    public class LogTableDataAdapter extends TableDataAdapter<Log> {

        public LogTableDataAdapter(Context context, List<Log> data) {
            super(context, data);
        }

        @Override
        public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
            Log log = getRowData(rowIndex);
            View renderedView = null;

            switch (columnIndex) {
                case 0:
                    renderedView = renderDate(0,log.getDate());
                    break;
                case 1:
                    renderedView = renderMsg(1,log.getMsg());
                    break;
            }

            return renderedView;
        }

    }




    /**
     * creates a view for table date
     * @param t title of view
     * @return view element
     */
    private View renderDate(int index, long t){

        TextView textView = new TextView(getContext());

        String date = dateFormatter.format(new Date(t));

        textView.setLines(2);
        String lines[] = date.split("--");

        if(lines.length == 2){
            textView.setText(lines[0] + "\n" + lines[1]);
        }

        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(14);
        return textView;
    }




    /**
     * creates a view for table msg
     * @param t title of view
     * @return view element
     */
    private View renderMsg(int index, String t){

        TextView textView = new TextView(getContext());
        textView.setText(t);

        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(14);
        return textView;
    }




    /**
     * Class representing comparator for logs
     */
    class LogComparator implements Comparator<Log> {
        @Override
        public int compare(Log log1, Log log2) {

            // compare the dates
            if (log1.getDate() > log2.getDate()) {
                return 1;
            } else if (log1.getDate() < log2.getDate()) {
                return -1;
            }
            return 0;
        }
    };



    /**
     * called when fragment id made visible to user
     */
    public void onResume(){
        super.onResume();

        refreshLogViews();
    }

}
