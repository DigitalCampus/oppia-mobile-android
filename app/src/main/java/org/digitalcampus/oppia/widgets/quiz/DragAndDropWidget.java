package org.digitalcampus.oppia.widgets.quiz;


import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;

import java.util.ArrayList;
import java.util.List;

import org.digitalcampus.mobile.quiz.model.questiontypes.DragAndDrop;

public class DragAndDropWidget extends QuestionWidget implements ViewTreeObserver.OnGlobalLayoutListener {

    private ViewGroup draggablesContainer, dropsContainer;

    private List<Dropzone> dropzones = new ArrayList<>();
    private List<Draggable> draggables = new ArrayList<>();
    private String courseLocation;



    private int backgroundWidth = 0, maxDragWidth = 0, maxDragHeight = 0;

    public DragAndDropWidget(Activity activity, View v, ViewGroup container, QuizQuestion q, String courseLocation) {
        super(activity, v, container, R.layout.widget_quiz_dragandrop);
        String dropzoneBackground = q.getProp("bgimage");
        this.courseLocation = courseLocation;

        String fileUrl = courseLocation + dropzoneBackground;
        Bitmap background = BitmapFactory.decodeFile(fileUrl);
        backgroundWidth = background.getWidth();
        ImageView dropzone = view.findViewById(R.id.dropzone_bg);
        dropzone.setImageBitmap(background);

        draggablesContainer = view.findViewById(R.id.drags_container);
        dropsContainer = view.findViewById(R.id.drops_container) ;

        // set up an observer that will be called once the layout is ready, to position the elements
        android.view.ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(this);
        }

        draggablesContainer.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        v.setBackgroundResource(R.drawable.dragscontainer_normal);
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundResource(R.drawable.dragscontainer_hover);
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackgroundResource(R.drawable.dragscontainer_normal);
                        return true;
                    case DragEvent.ACTION_DROP:
                        // Dropped, reassign View to ViewGroup
                        View view = (View) event.getLocalState();
                        ViewGroup owner = (ViewGroup) view.getParent();
                        owner.removeView(view);
                        ViewGroup container = (ViewGroup) v;
                        container.addView(view);
                        view.setVisibility(View.VISIBLE);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setBackgroundResource(R.drawable.dragscontainer_normal);
                        View v2 = (View) event.getLocalState();
                        v2.setVisibility(View.VISIBLE);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

    }
    @Override
    public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {

        for (Response r : responses){

            String type = r.getProp("type");
            if (DragAndDrop.TYPE_DROPZONE.equals(type)){

                String solution = r.getProp("choice");
                Dropzone drop = new Dropzone(ctx, solution);

                String xLeft= r.getProp("xleft");
                String yTop = r.getProp("ytop");
                if (xLeft != null && yTop != null){
                    drop.setPosition(Integer.parseInt(xLeft), Integer.parseInt(yTop));
                    drop.setOnDropListener(new OnDropListener() {
                        @Override
                        public void elemDropped(Draggable previousElem, Draggable newElem) {
                            if ((previousElem != null) && (!previousElem.isInfinite())){
                                draggablesContainer.addView(previousElem);
                            }
                        }
                    });
                    dropzones.add(drop);
                    dropsContainer.addView(drop);
                }
            }
            else if (DragAndDrop.TYPE_DRAGGABLE.equals(type)){
                String dragID = r.getProp("no");
                Draggable drag = new Draggable(ctx, dragID);
                String dragImage = r.getProp("dragimage");
                String infinite = r.getProp("infinite");
                if ((infinite != null) && (Integer.parseInt(infinite) == 1)){
                    drag.setInfinite(true);
                }
                if (dragImage != null){
                    dragImage = courseLocation + dragImage;
                    drag.setImagePath(dragImage);
                }
                draggables.add(drag);
            }
        }


        for (Draggable drag : draggables){
            boolean added = false;
            for (String answer : currentAnswers){
                String[] temp = answer.split(Quiz.MATCHING_REGEX,-1);
                if (temp.length < 2) continue;
                String dropzone = temp[0].trim();
                String draggable = temp[1].trim();

                if (drag.getDragID().equals(draggable)){
                    for (Dropzone drop : dropzones){
                        if (drop.getDragSolution().equals(dropzone)){
                            drop.addView(drag);
                            added = true;
                        }
                    }
                    break;
                }
            }

            if (!added){
                draggablesContainer.addView(drag);
            }
        }
        recalculateView();

    }

    @Override
    public List<String> getQuestionResponses(List<Response> responses) {

        List<String> userResponses = new ArrayList<>();
        for (Dropzone drop : dropzones){
            String response = drop.getResponse();
            if (response != null)
                userResponses.add(response);
        }
        return (userResponses.size() > 0 ? userResponses : null);
    }

    @Override
    public void onGlobalLayout() {
        recalculateView();
    }

    private void recalculateView(){
        int viewWidth = view.getMeasuredWidth();
        float ratio = (float) viewWidth / (float) backgroundWidth;

        for (Draggable drag : draggables){
            ViewGroup.LayoutParams params = drag.getLayoutParams();
            params.height = (int)(maxDragHeight * ratio);
            params.width = (int)(maxDragWidth * ratio);
        }

        for (Dropzone drop : dropzones){
            drop.repositionOnParent(ratio, maxDragHeight, maxDragWidth);
        }
    }

    interface OnDropListener{
        void elemDropped(Draggable previousElem, Draggable newElem);
    }


    class Draggable extends androidx.appcompat.widget.AppCompatImageView {

        private String dragID;
        private boolean infinite = false;
        private String imagePath;

        public Draggable(Context context) {
            super(context);
            this.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        public Draggable(Context context, String dropZone){
            this(context);
            this.dragID = dropZone;
        }

        public String getDragID() {
            return dragID;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
            Bitmap dragBg = BitmapFactory.decodeFile(imagePath);
            maxDragWidth = Math.max(maxDragWidth, dragBg.getWidth());
            maxDragHeight = Math.max(maxDragHeight, dragBg.getHeight());
            this.setImageBitmap(dragBg);
        }

        public boolean isInfinite() {
            return infinite;
        }

        public void setInfinite(boolean infinite) {
            this.infinite = infinite;
        }


        @Override
        public boolean onTouchEvent(MotionEvent motionEvent){
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(this);
                this.startDrag(data, shadowBuilder, this, 0);
                this.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }



    class Dropzone extends FrameLayout {

        private static final int activeState = R.drawable.dropzone_active;
        private static final int hoverState = R.drawable.dropzone_hover;

        private String dragSolution;
        private int topY;
        private int leftX;
        private OnDropListener onDropListener;

        public Dropzone(Context context, String dropzone) {
            super(context);
            this.dragSolution = dropzone;
        }

        public void setPosition(int startX, int startY) {
            this.leftX = startX;
            this.topY = startY;
        }

        public void setOnDropListener(OnDropListener listener){
            onDropListener = listener;
        }

        public String getDragSolution() {
            return dragSolution;
        }

        public String getResponse() {
            Draggable current = getCurrentDraggable();
            if (current != null){
                return dragSolution + Quiz.MATCHING_SEPARATOR + current.getDragID();
            }
            else{
                return null;
            }

        }

        public void repositionOnParent(float ratio, int height, int width){
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
            params.height = (int)(height * ratio);
            params.width = (int)(width * ratio);
            params.leftMargin = (int)(leftX * ratio);
            params.topMargin = (int)(topY * ratio);
        }

        private Draggable getCurrentDraggable(){
            if (this.getChildCount() > 0){
                return (Draggable) getChildAt(0);
            }
            return null;
        }

        @Override
        public boolean onDragEvent(DragEvent event) {

            Draggable draggable = (Draggable) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    setBackgroundResource(activeState);
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    setBackgroundResource(hoverState);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    setBackgroundResource(activeState);
                    return true;

                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    ViewGroup owner = (ViewGroup) draggable.getParent();
                    if (draggable.isInfinite()){
                       if (owner == draggablesContainer){
                           Draggable dragCopy = new Draggable(ctx, draggable.getDragID());
                           dragCopy.setImagePath(draggable.getImagePath());
                           dragCopy.setInfinite(draggable.isInfinite());
                           draggable = dragCopy;
                       }
                       else{
                           owner.removeView(draggable);
                       }
                    }
                    else{
                        owner.removeView(draggable);
                    }


                    Draggable previous = getCurrentDraggable();
                    this.removeAllViews();
                    this.addView(draggable);
                    draggable.setVisibility(View.VISIBLE);
                    onDropListener.elemDropped(previous, draggable);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    setBackgroundResource(0);
                    return true;
                default:
                    break;
            }
            return false;
        }


    }

}
