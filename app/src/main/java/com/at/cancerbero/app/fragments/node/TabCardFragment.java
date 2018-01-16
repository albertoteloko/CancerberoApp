package com.at.cancerbero.app.fragments.node;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.service.events.Event;

public class TabCardFragment extends TabFragment {


    private ListView listView;
    private Spinner spinnerWalk;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressDialog deletingDialog;

//    public void showItems(Set<Food> foods) {
//        if (listView != null) {
//            if (foods.isEmpty()) {
//                listView.setVisibility(View.GONE);
//            } else {
//                listView.setVisibility(View.VISIBLE);
//                listView.setItemChecked(-1, true);
//
//                List<Food> values = new ArrayList<>(foods);
//                listView.setAdapter(new MyFoodArrayAdapter(getContext(), values));
//            }
//        }
//    }

    @Override
    public boolean handle(Event event) {
        boolean result = false;

//        if (event instanceof FoodSearchDone) {
//            showItems(((FoodSearchDone) event).food);
//            swipeRefreshLayout.setRefreshing(false);
//            result = true;
//        }

        return result;
    }

//    private void acceptFood(Food itemValue) {
//        changeFragment(FoodDetailFragment.class);
//        ((FoodDetailFragment) getCurrentFragment()).acceptFood(itemValue);
//    }
//
//    private void foodDetails(Food itemValue) {
//        changeFragment(FoodDetailFragment.class);
//        ((FoodDetailFragment) getCurrentFragment()).loadFood(itemValue);
//    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        if (v.getId() == R.id.list_view_eat_food) {
//            super.onCreateContextMenu(menu, v, menuInfo);
//            menu.add(Menu.NONE, R.id.menu_accept_food, Menu.NONE, R.string.menu_accept_food);
//            menu.add(Menu.NONE, R.id.menu_detail_food, Menu.NONE, R.string.menu_details_food);
//        }
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        Food food = (Food) listView.getItemAtPosition(info.position);
//
//        switch (item.getItemId()) {
//            case R.id.menu_accept_food:
//                acceptFood(food);
//                return true;
//            case R.id.menu_detail_food:
//                foodDetails(food);
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);

        final View view = inflater.inflate(R.layout.fragment_tab_card, container, false);

//        listView = (ListView) view.findViewById(R.id.list_view_eat_food);
//        registerForContextMenu(listView);
//
//        spinnerWalk = (Spinner) view.findViewById(R.id.spinner_walk);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.walking_time, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerWalk.setAdapter(adapter);
//
//        spinnerWalk.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                runSearch(view);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        spinnerWalk.setSelection(2); // 15 min
//
//        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.layout_swipe);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                runSearch(view);
//            }
//        });
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                listView.showContextMenuForChild(view);
//            }
//
//        });
//
//        deletingDialog = new ProgressDialog(getActivity());
//        deletingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        deletingDialog.setMessage(getString(R.string.message_deleting_food));
//        deletingDialog.setIndeterminate(true);
//        deletingDialog.setCanceledOnTouchOutside(false);
//
//        runSearch(view);

        return view;
    }

//    private void runSearch(View view) {
//        String value = (String) spinnerWalk.getSelectedItem();
//        int minutes = Integer.parseInt(value.split(" ")[0]);
//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//            }
//        });
//        getMainService().searchFood(minutes, true);
//    }
}
