package com.chenyi.langeasy.list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.chenyi.langeasy.R;
import com.chenyi.langeasy.activity.MainNewActivity;
import com.chenyi.langeasy.fragment.PlayListFragment;
import com.chenyi.langeasy.listener.FragmentExchangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liyzh on 2016/9/10.
 */
public class SentenceAdapter extends ArrayAdapter<Map<String, Object>> {
    private ArrayList<Map<String, Object>> sentenceLst;
    public ArrayList<Map<String, Object>> mOriginalValues; // Original Values
    private ArrayList<Map<String, Object>> mSecondLevelValues; // second level search Values
    public ArrayList<Integer> sentenceIdList;
    private PlayListFragment.AdapterCallback mAdapterCallback;
    private View listLayout;
    private Context mContext;

    public SentenceAdapter(Context context, View listLayout, ArrayList<Map<String, Object>> sentenceLst) {
        super(context, 0, sentenceLst);

        this.mContext = context;
        this.sentenceLst = sentenceLst;
        this.listLayout = listLayout;
    }

    public SentenceAdapter(Context context, View listLayout, PlayListFragment.AdapterCallback adapterCallback, ArrayList<Map<String, Object>> sentenceLst) {
        super(context, 0, sentenceLst);
        this.mAdapterCallback = adapterCallback;

        this.mContext = context;
        this.sentenceLst = sentenceLst;
        this.listLayout = listLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Map<String, Object> sentence = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_item, parent, false);
        }
        // Lookup view for data population
        TextView songTitle = (TextView) convertView.findViewById(R.id.songTitle);
//        TextView tvHome = (TextView) convertView.findViewById(R.id.tvHome);

        // Populate the data into the template view using the data object
        String text = (String) sentence.get("wordunique");

        if (sentence.get("index") != null) {
            text = ((int) sentence.get("index") + 1) + "/" + text;
        } else {
            text = (position + 1) + "/" + text;
        }
        songTitle.setText(text);

        TextView vBooktype = (TextView) convertView.findViewById(R.id.booktype);
        String booktype = (String) sentence.get("booktype");
        vBooktype.setText(booktype + "[n" + sentence.get("scount") + "]");

        TextView vBookname = (TextView) convertView.findViewById(R.id.bookname);
        String bookname = (String) sentence.get("bookname");
        vBookname.setText(bookname);
        // Return the completed view to render on screen
        return convertView;
    }

    public int filterLevel = 1;

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
//                sentenceLst = (ArrayList<Map<String, Object>>) results.values; // has the filtered values
                if (filterLevel == 1) {

                }
                ((MainNewActivity) mContext).stopPlay();

                sentenceLst.clear();
                sentenceLst.addAll((ArrayList<Map<String, Object>>) results.values);
                notifyDataSetChanged();  // notifies the data with new filtered values
                if (mAdapterCallback != null) {
                    mAdapterCallback.filterFinished();
                }


                TextView vSize = (TextView) listLayout.findViewById(R.id.size_val);
                vSize.setText(sentenceLst.size() + "");
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                ArrayList<Map<String, Object>> FilteredArrList = new ArrayList<>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Map<String, Object>>(sentenceLst); // saves the original data in mOriginalValues
//                    mOriginalValues = new ArrayList<Map<String, Object>>(); // deep copy list
//                    for(Map<String, Object> sentence: sentenceLst){
//                        Map<String, Object> shallowCopy = new HashMap<String, Object>();
//                        shallowCopy.putAll(sentence);// shallow copy map
//                        mOriginalValues.add(shallowCopy);
//                    }
                }

                /********
                 *
                 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
                 *  else does the Filtering and returns FilteredArrList(Filtered)
                 *
                 ********/
                if (constraint == null || constraint.length() == 0) {
                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    String condition = constraint.toString().toLowerCase();
                    int count = 0;
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        Map<String, Object> data = mOriginalValues.get(i);

                        if (condition.startsWith("bt:")) {// query by book
                            String bt = condition.substring(3);
//                            Log.i("condition", condition);
//                            Log.i("bt", bt);
                            String booktype = (String) data.get("booktype");
                            booktype = booktype.toLowerCase().trim();
//                            Log.i("booktype", booktype);
                            if (booktype.equals(bt)) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("b:")) {// query by book
                            String bid = condition.substring(2);
//                            Log.i("condition", condition);
//                            Log.i("bid", bid);
                            String bookid = (String) data.get("bookid");
//                            Log.i("bookid", bookid);
                            if (bookid.equals(bid)) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("c:")) {// query by book
                            String cid = condition.substring(2);
//                            Log.i("condition", condition);
//                            Log.i("cid", cid);
                            String courseid = (String) data.get("courseid");
//                            Log.i("courseid", courseid);
                            if (courseid.equals(cid)) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("qid:")) {// filter by queue
                            Integer sentenceid = (Integer) data.get("sentenceid");
                            if (containSid(sentenceid)) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("[critical50]")) {// query sentence list that listened-count not less than 50
                            Integer scount = (Integer) data.get("scount");
                            if (scount >= 50) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("[critical30]")) {// query sentence list that listened-count between 30 and 50
                            Integer scount = (Integer) data.get("scount");
                            if (scount >= 30 && scount < 50) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("[critical20]")) {// query sentence list that listened-count between 20 and 30
                            Integer scount = (Integer) data.get("scount");
                            if (scount >= 20 && scount < 30) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("[critical10]")) {// query sentence list that listened-count between 10 and 20
                            Integer scount = (Integer) data.get("scount");
                            if (scount >= 10 && scount < 20) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else if (condition.startsWith("[critical0]")) {// query sentence list that listened-count below 10
                            Integer scount = (Integer) data.get("scount");
                            if (scount < 10) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        } else {
                            String wordunique = (String) data.get("wordunique");
                            if (wordunique.indexOf(condition) > -1) {
                                data.put("index", count++);
                                FilteredArrList.add(data);
                            }
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;

                }
                return results;
            }
        };
        return filter;
    }

    private boolean containSid(Integer sid) {
        for (Integer sid2 : sentenceIdList) {
            if (sid.equals(sid2)) {
                return true;
            }
        }
        return false;
    }
}
