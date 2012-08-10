package org.digitalcampus.mtrain.adapter;


import java.util.List;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModuleListAdapter extends ArrayAdapter<Module> {

	private LayoutInflater inflater; 
	
	public ModuleListAdapter(Activity context, List<Module> moduleList) {
		super( context, R.layout.module_list_row, R.id.module_title, moduleList ); 
		inflater = LayoutInflater.from(context) ; 
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Module m = (Module) this.getItem( position ); 
		//CheckBox checkBox ;   
	    TextView textView ;  
		
	    if ( convertView == null ) {  
	        convertView = inflater.inflate(R.layout.module_list_row, null);  
	          
	        // Find the child views.  
	        textView = (TextView) convertView.findViewById( R.id.module_title );    
	          
	        // Optimization: Tag the row with it's child views, so we don't have to   
	        // call findViewById() later when we reuse the row.  
	        convertView.setTag( new ModuleViewHolder(textView) );  
	  
	        // If CheckBox is toggled, update the planet it is tagged with.  
	       // checkBox.setOnClickListener( new View.OnClickListener() {  
		     //     public void onClick(View v) {  
		           // CheckBox cb = (CheckBox) v ;  
		            //Module m = (Module) cb.getTag();  
		            //q.setChecked( cb.isChecked() );  
		      //    }  
		      //  });          
	      } else  {  
	          // Because we use a ViewHolder, we avoid having to call findViewById().  
	    	  ModuleViewHolder viewHolder = (ModuleViewHolder) convertView.getTag();  
	          textView = viewHolder.getTextView() ;  
	        }  
	    
	        // Tag the CheckBox with the Planet it is displaying, so that we can  
	        // access the planet in onClick() when the CheckBox is toggled.  
	          
	        // Display planet data  
	        textView.setText( m.getTitle() );        
	          
	        return convertView;  
	      }  
	
	private static class ModuleViewHolder {    
	    private TextView textView ;  
	    public ModuleViewHolder() {}  
	    
	    public ModuleViewHolder( TextView textView ) {  
	      this.textView = textView ;  
	    }  

	    public TextView getTextView() {  
	      return textView;  
	    }  
	    public void setTextView(TextView textView) {  
	      this.textView = textView;  
	    }      
	  } 
}
