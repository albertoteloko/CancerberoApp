package com.at.cancerbero.app.fragments.node;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.CardEntriesAdapter;
import com.at.cancerbero.domain.model.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class TabCardFragment extends TabFragment {

    private String nodeId;

    private ListView listView;

    private Map<String, String> cardEntries;

    @Override
    public void showItem(Node node) {
        nodeId = node.id;
        cardEntries = getCardMap(node);

        List<Map.Entry<String, String>> entries = getCardEntries(node);
        if (entries.isEmpty()) {
            listView.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.VISIBLE);
            listView.setItemChecked(-1, true);

            listView.setAdapter(new CardEntriesAdapter(getContext(), entries));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(false);

        final View view = inflater.inflate(R.layout.fragment_tab_card, container, false);

        listView = view.findViewById(R.id.list_cards);
        registerForContextMenu(listView);

        getNodeFragment().addNodeListener(this::showItem);
        getNodeFragment().addCardListener(this::onCardIdRead);

        return view;
    }

    private List<Map.Entry<String, String>> getCardEntries(Node node) {
        return StreamSupport.stream(getCardMap(node).entrySet())
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .collect(Collectors.toList());
    }

    private Map<String, String> getCardMap(Node node) {
        Map<String, String> entries = new HashMap<>();

        if ((node.modules.card != null) && (node.modules.card.entries != null)) {
            entries = node.modules.card.entries;

        }

        return entries;
    }

    private void onCardIdRead(String cardId) {
        if (cardEntries.containsKey(cardId)) {
            showRemoveCardEntryDialog(cardId);
        } else {
            showAddCardEntryDialog(cardId);
        }
    }

    private void showRemoveCardEntryDialog(String cardId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.remove_card)
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    dialog.dismiss();
                    removeCardEntry(cardId);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                }).show();
    }

    private void showAddCardEntryDialog(String cardId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.add_new_card);
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.layout_text_input_dialog, (ViewGroup) getView(), false);

        final EditText input = viewInflated.findViewById(R.id.input);
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            String name = input.getText().toString();
            addCardEntry(cardId, name);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.cancel();
        });

        builder.show();
    }

    private void addCardEntry(String cardId, String name) {

    }


    private void removeCardEntry(String cardId) {

    }
}
