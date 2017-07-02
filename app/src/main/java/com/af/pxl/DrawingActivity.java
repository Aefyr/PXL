package com.af.pxl;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class DrawingActivity extends AppCompatActivity {

    AdaptivePixelSurface aps;
    ImageButton toolButton;
    String[] tools;
    AlertDialog toolPickDialog;
    ImageButton.OnClickListener onClickListener;
    Button cursorAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        aps = (AdaptivePixelSurface) findViewById(R.id.aps);
        aps.setColorCircle((ColorCircle)findViewById(R.id.color));

        initializeImageButtonsOCL();
        initializeToolPicking();
        initializeCursor();


    }

    private void initializeToolPicking(){
        final Resources res = getResources();

        tools = new String[]{res.getString(R.string.pencil), res.getString(R.string.fill), res.getString(R.string.colorpick)};

        toolButton = (ImageButton) findViewById(R.id.currentTool);

        toolPickDialog = new AlertDialog.Builder(this).setItems(tools, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        aps.currentTool = AdaptivePixelSurface.Tool.PENCIL;
                        toolButton.setImageResource(R.drawable.pencil);
                        cursorAction.setText(res.getString(R.string.pencil_action));
                        break;
                    case 1:
                        aps.currentTool = AdaptivePixelSurface.Tool.FLOOD_FILL;
                        toolButton.setImageResource(R.drawable.fill);
                        cursorAction.setText(res.getString(R.string.fill_action));
                        break;
                    case 2:
                        aps.currentTool = AdaptivePixelSurface.Tool.COLOR_PICK;
                        toolButton.setImageResource(R.drawable.colorpick);
                        cursorAction.setText(res.getString(R.string.colorpick_action));
                        break;

                }
            }
        }).create();

        toolButton.setOnClickListener(onClickListener);
    }

    private void initializeImageButtonsOCL(){
        onClickListener = new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.currentTool:
                        toolPickDialog.show();
                        break;
                    case R.id.cursorMode:
                        aps.cursor.setEnabled(!aps.cursorMode);
                        break;
                }
            }
        };
    }

    private void initializeCursor(){
        cursorAction = (Button) findViewById(R.id.cursorAction);
        cursorAction.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    aps.cursor.cursorDown();
                if(motionEvent.getAction() == MotionEvent.ACTION_UP)
                    aps.cursor.cursorUp();
                return false;
            }
        });

        (findViewById(R.id.cursorMode)).setOnClickListener(onClickListener);

        aps.cursor.setOnCursorChangeListener(new Cursor.OnCursorChangeListener() {
            @Override
            public void onCursorEnabled(boolean enabled) {
                if(enabled){
                    cursorAction.setVisibility(View.VISIBLE);
                }else
                    cursorAction.setVisibility(View.GONE);
            }
        });
    }

}
