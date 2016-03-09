package com.blakit.petrenko.habits;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.blakit.petrenko.habits.model.Action;
import com.blakit.petrenko.habits.utils.Resources;
import com.blakit.petrenko.habits.utils.Utils;

import java.util.ListIterator;

import io.realm.RealmList;

/**
 * Created by user_And on 11.02.2016.
 */
public class CreateDaysAdapter extends RecyclerView.Adapter<CreateDaysAdapter.DaysViewHolder>{

    private Context context;
    private RecyclerView parent;
    private EditText defaultActions;
    private RealmList<Action> actions;

    private boolean isSavePressed;
    private DayDialog dialog;

    private float sp;

    private final int dayTextSp = 14;
    private final int dayCircleSp = 34;

    public CreateDaysAdapter(Context context, RecyclerView parent, EditText defaultActions, RealmList<Action> actions) {
        this.context = context;
        this.parent = parent;
        this.defaultActions = defaultActions;
        this.actions = actions;

        isSavePressed = false;
        dialog = new DayDialog();

        sp = context.getResources().getDisplayMetrics().scaledDensity;

//        updateRecyclerViewHeight();
    }

    @Override
    public DaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.create_day_item, parent, false);
        return new DaysViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DaysViewHolder holder, int position) {
        final Action action = Utils.getAction(actions, position + 1);

        String text = ""+action.getDay();
        int colors[] = getColors(action);

        TextDrawable numberDr = TextDrawable.builder()
                .beginConfig()
                    .textColor(colors[0])
                    .useFont(Resources.getInstance().getTypeface("OpenSans-Light"))
                    .fontSize((int) (dayTextSp * sp))
                .endConfig()
                .buildRoundRect(text, colors[1], (int) (dayCircleSp * sp));

        ViewGroup.LayoutParams params = holder.number.getLayoutParams();
        params.height = (int) (dayCircleSp * sp);
        params.width  = (int) (dayCircleSp * sp);

        holder.view.getLayoutParams().height = params.height;

        holder.number.setImageDrawable(numberDr);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show(action);
            }
        });
    }

    @Override
    public int getItemCount() {
//        updateRecyclerViewHeight();
        return actions.size();
    }


    public void addWeek() {
        int size = actions.size();

        if (size + 7 <= 98) {
            for (int i = 1; i <= 7; ++i) {
                Action a = new Action("", size+i);
                a.setUseDefault(true);
                actions.add(a);
            }
            notifyDataSetChanged();
        }
    }


    public void removeWeek() {
        int size = actions.size();
        int removeCount = 7;

        if (size >= 28) {
            ListIterator<Action> it = actions.listIterator(actions.size());

            while (it.hasPrevious() && removeCount > 0) {
                if (it.previous().getDay() > size - 7) {
                    it.remove();
                    --removeCount;
                }
            }

            notifyDataSetChanged();
        }
    }


    private int[] getColors(Action action) {
        if (action.isSkipped()) {
            return new int[]{
                ContextCompat.getColor(context, R.color.md_grey_200),
                ContextCompat.getColor(context, R.color.md_grey_100)
            };
        } else if (action.isUseDefault()) {
            return new int[] {
                ContextCompat.getColor(context, R.color.md_grey_700),
                ContextCompat.getColor(context, R.color.md_grey_200)
            };
        } else {
            if (isSavePressed && TextUtils.isEmpty(action.getAction())) {
                return new int[] {
                    ContextCompat.getColor(context, R.color.md_white_1000),
                    ContextCompat.getColor(context, R.color.md_red_400)
                };
            } else {
                return new int[] {
                    ContextCompat.getColor(context, R.color.md_white_1000),
                    ContextCompat.getColor(context, R.color.md_grey_600)
                };
            }
        }
    }


//    public void updateRecyclerViewHeight() {
//        int rowCount = 1;
//        RecyclerView.LayoutManager manager = parent.getLayoutManager();
//
//        if (manager != null && manager instanceof GridLayoutManager) {
//            rowCount = (actions.size() - 1) / ((GridLayoutManager) manager).getSpanCount() + 1;
//        }
//
//        int magicNumber = Utils.dpToPx(context, 5);//(int) Math.max(Utils.dpToPx(context, 5), 5 * sp);
//
//        ViewGroup.LayoutParams params = parent.getLayoutParams();
//        params.height = rowCount * (Utils.dpToPx(context, 10) + (int)(dayCircleSp  * sp) + magicNumber);
//    }


    public void setSavePressed(boolean isSavePressed) {
        this.isSavePressed = isSavePressed;
    }


    public void showDialog(int day) {
        if (!dialog.isShowing() && day >= 0) {
            dialog.show(Utils.getAction(actions, day));
        }
    }


    public int getShowingDay() {
        return dialog.isShowing() ? dialog.currentAction.getDay() : -1;
    }


    public class DaysViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView number;

        public DaysViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            number = (ImageView) itemView.findViewById(R.id.create_day_item_number);
        }
    }


    private class DayDialog {
        private AlertDialog dialog;
        private Action currentAction;

        private EditText actionEdit;
        private TextView actionWarning;
        private CheckBox useDefaultCheck;
        private CheckBox skippedCheck;

        private int textColor = ContextCompat.getColor(context, R.color.md_grey_600);

        public DayDialog() {
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_day_action, null, false);

            actionEdit      = (EditText) view.findViewById(R.id.create_habit_day_dialog_action);
            actionWarning   = (TextView) view.findViewById(R.id.create_habit_day_dialog_action_warning);
            useDefaultCheck = (CheckBox) view.findViewById(R.id.create_habit_day_dialog_use_default);
            skippedCheck    = (CheckBox) view.findViewById(R.id.create_habit_day_dialog_skipped);

            actionEdit.addTextChangedListener(new TextWatcherAdapter(){
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (TextUtils.isEmpty(s) && !useDefaultCheck.isChecked()) {
                        actionWarning.setVisibility(View.VISIBLE);
                    } else {
                        actionWarning.setVisibility(View.GONE);
                    }
                }
            });

            useDefaultCheck.setTypeface(Resources.getInstance().getTypeface("OpenSans-Light"));
            skippedCheck.setTypeface(useDefaultCheck.getTypeface());

            useDefaultCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    actionEdit.setEnabled(!isChecked && !skippedCheck.isChecked());
                    if (isChecked) {
                        actionEdit.setText(defaultActions.getText().toString());
                    }
                }
            });
            skippedCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    actionEdit.setEnabled(!isChecked && !useDefaultCheck.isChecked());
                }
            });

            ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, R.style.AlertDialogTheme);
            dialog = new AlertDialog.Builder(themeWrapper)
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentAction.setAction(actionEdit.getText().toString());
                            currentAction.setSkipped(skippedCheck.isChecked());
                            currentAction.setUseDefault(useDefaultCheck.isChecked());
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .setCancelable(true)
                    .create();
        }


        public void show(Action currentAction) {
            if (currentAction == null) {
                return;
            }
            this.currentAction = currentAction;

            if (useDefaultCheck.isChecked()) {
                currentAction.setAction(defaultActions.getText().toString());
            }
            actionEdit.setText(currentAction.getAction());

            skippedCheck.setChecked(currentAction.isSkipped());
            useDefaultCheck.setChecked(currentAction.isUseDefault());

            dialog.setTitle(context.getString(R.string.day)+" "+ currentAction.getDay());
            dialog.show();
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(textColor);
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(textColor);
        }

        public boolean isShowing() {
            return dialog.isShowing();
        }


    }
}
