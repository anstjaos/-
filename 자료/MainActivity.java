package com.example.document.layouttest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText nameEditText, endDateEditText,descriptionEditText, itemEditText;
    EditText[] itemList;
    Button createNewItemBtn, registerBtn, cancelBtn;
    LinearLayout itemLayout;
    private int itemListCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemList = new EditText[20];

        nameEditText = (EditText)findViewById(R.id.NameEditText);
        endDateEditText = (EditText)findViewById(R.id.EndDateEditText);
        descriptionEditText = (EditText)findViewById(R.id.DescriptionEditText);
        itemEditText = (EditText) findViewById(R.id.ItemEditText);

        createNewItemBtn = (Button) findViewById(R.id.CreateNewItemBtn);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);

        itemLayout = (LinearLayout) findViewById(R.id.ItemLayout);

        itemList[itemListCnt++] = itemEditText;
        createNewItemBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(itemListCnt >= 20){
                    Toast.makeText(getApplicationContext(), "더 이상 추가할 수 없습니다!", Toast.LENGTH_LONG).show();
                    return;
                }

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                final EditText editText = new EditText(getApplicationContext());

                float mScale = getResources().getDisplayMetrics().density;
                final int width = (int)(180*mScale);

                editText.setText("");
                editText.setWidth(width);
                editText.setTextSize(20);
                editText.setLayoutParams(params);
                itemLayout.addView(editText);

                itemList[itemListCnt++] = editText;
            }
        });

        registerBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "등록 완료", Toast.LENGTH_LONG).show();

                String msg = "";
                for(int i = 0 ; i < itemListCnt; i++)
                    msg += itemList[i].getText() + "\n";
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        cancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "취소 완료", Toast.LENGTH_LONG).show();
            }
        });
    }

}