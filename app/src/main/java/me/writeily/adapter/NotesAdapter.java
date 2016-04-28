package me.writeily.adapter;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.writeily.R;
import me.writeily.model.Constants;

/**
 * Created by jeff on 2014-04-11.
 */
public class NotesAdapter extends ArrayAdapter<File> implements Filterable {

    public static final String EMPTY_STRING = "";
    private Context context;
    private List<File> data;
    private List<File> filteredData;

    public NotesAdapter(Context context, int resource, List<File> objects) {
        super(context, resource, objects);
        this.context = context;
        this.data = objects;
        this.filteredData = data;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public File getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static class ViewHolderItem {
        TextView noteTitle;
        TextView noteExtra;
        ImageView fileIdentifierImageView;
        String theme;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolderItem viewHolder;
        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.file_item, viewGroup, false);

            viewHolder = new ViewHolderItem();
            viewHolder.theme = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_theme_key), "");
            viewHolder.noteTitle = (TextView) convertView.findViewById(R.id.note_title);
            viewHolder.noteExtra = (TextView) convertView.findViewById(R.id.note_extra);
            viewHolder.fileIdentifierImageView = (ImageView) convertView.findViewById(R.id.file_identifier_icon);

            convertView.setTag(viewHolder);
        }
        else{
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        viewHolder.noteTitle.setText(Constants.MD_EXTENSION.matcher(getItem(i).getName()).replaceAll(EMPTY_STRING));

        if (getItem(i).isDirectory()) {
            viewHolder.noteExtra.setText(generateExtraForFile(i));
        } else {
            viewHolder.noteExtra.setText(generateExtraForDirectory(i));
        }

        // Theme Adjustments
        if (viewHolder.theme.equals(context.getString(R.string.theme_dark))) {
            viewHolder.noteTitle.setTextColor(context.getResources().getColor(android.R.color.white));

            if (getItem(i).isDirectory()) {
                viewHolder.fileIdentifierImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder_light));
            } else {
                viewHolder.fileIdentifierImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notes_light));
            }
        } else {
            viewHolder.noteTitle.setTextColor(context.getResources().getColor(R.color.dark_grey));
            if (getItem(i).isDirectory()) {
                viewHolder.fileIdentifierImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder));
            } else {
                viewHolder.fileIdentifierImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notes));
            }
        }
        return convertView;
    }

    private String generateExtraForFile(int i) {
        int fileAmount = ((getItem(i).listFiles() == null) ? 0 : getItem(i).listFiles().length);
        return String.format(context.getString(R.string.number_of_files), fileAmount);
    }

    private String generateExtraForDirectory(int i) {
        String formattedDate = DateUtils.formatDateTime(context, getItem(i).lastModified(),
                (DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NUMERIC_DATE));
        return String.format(context.getString(R.string.last_modified), formattedDate);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults searchResults = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    searchResults.values = data;
                    searchResults.count = data.size();
                } else {
                    ArrayList<File> searchResultsData = new ArrayList<File>();

                    for (File item : data) {
                        if (item.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            searchResultsData.add(item);
                        }
                    }

                    searchResults.values = searchResultsData;
                    searchResults.count = searchResultsData.size();
                }
                return searchResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<File>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}