package com.at.cancerbero.app.fragments.node;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.ImageUtils;
import com.at.cancerbero.app.fragments.AppFragment;
import com.at.cancerbero.app.fragments.installation.InstallationFragment;
import com.at.cancerbero.domain.model.AlarmStatus;
import com.at.cancerbero.domain.model.Node;

import java.util.ArrayList;
import java.util.List;

public class NodeFragment extends AppFragment implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private static final String TAB_SELECTED = "TAB_SELECTED";

    private TextView nodeName;

    private ImageView statusImage;

    private ListView listView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String nodeId;

    private TabLayout tabLayout;

    private ViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public NodeFragment() {
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void showItem(Node node) {
        getMainActivity().setActivityTitle(node.name);
        if(nodeName != null){
            nodeName.setText(node.name);
        }

        if(statusImage != null){
            statusImage.setImageResource(getImage(node));
        }
//        if (listView != null) {
//            if (node.nodes.isEmpty()) {
//                listView.setVisibility(View.GONE);
//            } else {
//                listView.setVisibility(View.VISIBLE);
//                listView.setItemChecked(-1, true);
//
//                List<Node> values = new ArrayList<>(node.nodes);
//                listView.setAdapter(new NodeAdapter(getContext(), values));
//            }
//        }
    }

    private int getImage(Node node) {
        AlarmStatus status = AlarmStatus.IDLE;

        if ((node.modules.alarm != null) && (node.modules.alarm.status != null)) {
            status = node.modules.alarm.status.value;
        }

        return ImageUtils.getImage(status);
    }

    @Override
    public View onCreateViewApp(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_node, container, false);

        getSupportActionBar().show();

        nodeName = view.findViewById(R.id.node_name);

        statusImage = view.findViewById(R.id.node_status);

//        listView = view.findViewById(R.id.list_pins);
//        registerForContextMenu(listView);

        swipeRefreshLayout = view.findViewById(R.id.layout_swipe);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNode(view);
            }
        });

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) view.findViewById(R.id.pager_viewer);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(this);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                listView.showContextMenuForChild(view);
//            }
//
//        });

        tabLayout = view.findViewById(R.id.layout_tabs);
        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.addOnTabSelectedListener(this);

        tabLayout.addTab(createTab(tabLayout, R.string.title_tab_alarm, R.drawable.ic_settings_remote_black_24dp));
        tabLayout.addTab(createTab(tabLayout, R.string.title_tab_card, R.drawable.ic_payment_black_24dp));

        loadNode(view);

        getMainActivity().setActivityTitle(R.string.title_node);

        return view;
    }

    private TabLayout.Tab createTab(TabLayout tabLayout, Integer title, Integer icon) {
        TabLayout.Tab result = tabLayout.newTab();

        if (title != null) {
            result.setText(title);
        }

        if (icon != null) {
            result.setIcon(icon);
        }

        return result;
    }

    private void loadNode(View view) {
        if (nodeId != null) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
            getMainService().loadNode(nodeId);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (nodeId != null) {
            outState.putString("nodeId", nodeId);
        }

        outState.putInt(TAB_SELECTED, tabLayout.getSelectedTabPosition());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if((savedInstanceState != null)  && (savedInstanceState.containsKey("nodeId"))){
            nodeId = savedInstanceState.getString("nodeId");
            loadNode(getView());

        }
            int tabIndex = 0;

            if (savedInstanceState != null) {
                tabIndex = savedInstanceState.getInt(TAB_SELECTED, 0);
            }
            selectTab(tabIndex);
    }

    private void selectTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);

        if (tab != null) {
            tab.select();
        }

    }


    @Override
    public boolean onBackPressed() {
        changeFragment(InstallationFragment.class);
        return true;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        mViewPager.setCurrentItem(-1);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        selectTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<TabFragment> items = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            items.add(new TabAlarmFragment());
            items.add(new TabCardFragment());

            for (TabFragment item : items) {
                item.setTabsFragment(NodeFragment.this);
            }
        }


        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }
    }
}