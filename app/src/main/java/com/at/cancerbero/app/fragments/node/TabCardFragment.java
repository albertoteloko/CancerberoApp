package com.at.cancerbero.app.fragments.node;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.at.cancerbero.CancerberoApp.R;
import com.at.cancerbero.adapter.CardEntriesAdapter;
import com.at.cancerbero.domain.model.AlarmStatus;
import com.at.cancerbero.domain.model.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import java8.util.concurrent.CompletableFuture;
import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

public class TabCardFragment extends TabFragment {

    private String nodeId;

    private ListView listView;

    private Map<String, String> cardEntries;

    private AlertDialog alertDialog;

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
        closeAlertDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        String text = getMainActivity().getResources().getString(R.string.remove_card);
        alertDialog = builder.setMessage(text + " " +cardEntries.get(cardId) + "?")
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    dialog.dismiss();
                    removeCardEntry(cardId);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.cancel();
                }).show();
    }

    private void showAddCardEntryDialog(String cardId) {
        closeAlertDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        builder.setTitle(R.string.add_new_card);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.dismiss();
            String name = input.getText().toString();
            addCardEntry(cardId, name);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.cancel();
        });

        alertDialog = builder.show();
    }

    private void closeAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    private void addCardEntry(String cardId, String name) {
        CompletableFuture<Boolean> future = getMainService().getInstallationService().addCard(nodeId, cardId, name);
        ProgressDialog dialog = showProgressMessage(R.string.label_adding_card);

        future.handle((v, t) -> {
            runOnUiThread(() -> {
                if (t != null) {
                    showToast(R.string.label_unable_to_perform_action);
                    Log.e(TAG, "Unable to add card", t);
                } else {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                loadNode();
                            });
                        }
                    }, 500);
                }
                dialog.dismiss();
            });
            return null;
        });
    }


    private void removeCardEntry(String cardId) {
        CompletableFuture<Boolean> future = getMainService().getInstallationService().removeCard(nodeId, cardId);
        ProgressDialog dialog = showProgressMessage(R.string.label_removing_card);

        future.handle((v, t) -> {
            runOnUiThread(() -> {
                if (t != null) {
                    showToast(R.string.label_unable_to_perform_action);
                    Log.e(TAG, "Unable to remove card", t);
                } else {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(() -> {
                                loadNode();
                            });
                        }
                    }, 500);
                }
                dialog.dismiss();
            });
            return null;
        });
    }
}
