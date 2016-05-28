/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */


package org.digitalcampus.oppia.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.CourseTag;

import java.util.List;

public class CourseCategoriesAdapter extends ArrayAdapter<CourseTag> {

    private final Context ctx;
    private List<CourseTag> tagList;

    public CourseCategoriesAdapter(Context context, List<CourseTag> objects) {
        super(context, R.layout.category_list_row, objects);
        this.ctx = context;
        this.tagList = objects;
    }

    static class CategoryViewHolder{
        TextView categoryTitle;
        TextView tagCount;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CategoryViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.category_list_row, parent, false);
            viewHolder = new CategoryViewHolder();
            viewHolder.categoryTitle = (TextView) convertView.findViewById(R.id.tag_title);
            viewHolder.tagCount = (TextView) convertView.findViewById(R.id.tag_count);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (CategoryViewHolder) convertView.getTag();
        }

        CourseTag c = tagList.get(position);
        viewHolder.categoryTitle.setText(c.getTag());
        viewHolder.tagCount.setText(""+c.getCount());

        return convertView;
    }
}

