package com.example.artistlan.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Conector.model.ChatbotActionDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeKeys;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.model.ChatbotMessageUi;
import com.example.artistlan.utils.CardThemeHelper;
import com.google.android.material.card.MaterialCardView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChatbotMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_BOT = 2;

    public interface ChatbotInteractionListener {
        void onQuickReplyClick(@NonNull String text);

        void onActionClick(@NonNull ChatbotActionDTO action);

        boolean shouldShowAction(@NonNull ChatbotActionDTO action);
    }

    private final List<ChatbotMessageUi> items = new ArrayList<>();
    private final ChatbotInteractionListener listener;

    public ChatbotMessageAdapter(@NonNull ChatbotInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatbotMessageUi item = items.get(position);
        return item != null && item.isFromUser() ? TYPE_USER : TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chatbot_message_user, parent, false);
            return new UserViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_chatbot_message_bot, parent, false);
        return new BotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatbotMessageUi item = items.get(position);
        if (holder instanceof UserViewHolder) {
            bindUser((UserViewHolder) holder, item);
            return;
        }
        bindBot((BotViewHolder) holder, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addMessage(@NonNull ChatbotMessageUi message) {
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    public void removeMessage(@NonNull ChatbotMessageUi message) {
        int index = items.indexOf(message);
        if (index >= 0) {
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    private void bindUser(@NonNull UserViewHolder holder, @NonNull ChatbotMessageUi item) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        holder.tvMessage.setText(item.getText());
        UserBubbleStyle bubbleStyle = resolveUserBubbleStyle(tm);
        holder.tvMessage.setTextColor(ColorUtils.setAlphaComponent(bubbleStyle.textColor, 255));
        holder.tvMessage.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        holder.tvMessage.setAlpha(1f);
        holder.cardBubble.setAlpha(1f);
        applyUserBubble(holder, bubbleStyle);
    }

    private void bindBot(@NonNull BotViewHolder holder, @NonNull ChatbotMessageUi item) {
        ThemeManager tm = new ThemeManager(holder.itemView.getContext());
        holder.tvMessage.setText(item.getText());
        holder.tvMessage.setTypeface(Typeface.create("sans-serif", item.isLoading() ? Typeface.ITALIC : Typeface.NORMAL));
        ThemeApplier.applyTextPrimary(holder.tvMessage, tm);
        int background = item.isLoading()
                ? ColorUtils.blendARGB(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), tm.color(ThemeKeys.BUTTON_SECONDARY_BG), 0.20f)
                : tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL);
        int stroke = item.isLoading()
                ? tm.color(ThemeKeys.ACCENT_PRIMARY)
                : ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 210);
        styleBubbleCard(holder.cardBubble, background, stroke, 20, item.isLoading() ? 2 : 1);

        List<String> quickReplies = item.getQuickReplies();
        List<ChatbotActionDTO> actions = item.getActions();
        List<ChatbotActionDTO> visibleActions = visibleActions(actions);
        Set<String> visibleActionKeys = actionComparisonKeys(visibleActions);
        holder.quickRepliesContainer.removeAllViews();
        holder.actionsContainer.removeAllViews();

        if (item.isLoading()) {
            holder.quickRepliesWrapper.setVisibility(View.GONE);
            holder.actionsWrapper.setVisibility(View.GONE);
            return;
        }

        if (quickReplies == null || quickReplies.isEmpty()) {
            holder.quickRepliesWrapper.setVisibility(View.GONE);
        } else {
            holder.quickRepliesWrapper.setVisibility(View.VISIBLE);
            LinearLayout currentRow = null;
            int chipsInRow = 0;
            for (String quickReply : quickReplies) {
                if (quickReply == null || quickReply.trim().isEmpty()) {
                    continue;
                }
                String cleanQuickReply = quickReply.trim();
                if (shouldHideQuickReply(cleanQuickReply, visibleActionKeys)) {
                    continue;
                }
                if (currentRow == null || chipsInRow >= 2) {
                    currentRow = createQuickReplyRow(holder.quickRepliesContainer);
                    holder.quickRepliesContainer.addView(currentRow);
                    chipsInRow = 0;
                }
                TextView chip = createQuickReplyChip(currentRow, cleanQuickReply, tm);
                currentRow.addView(chip);
                chipsInRow++;
            }

            if (holder.quickRepliesContainer.getChildCount() == 0) {
                holder.quickRepliesWrapper.setVisibility(View.GONE);
            }
        }

        if (visibleActions.isEmpty()) {
            holder.actionsWrapper.setVisibility(View.GONE);
            return;
        }

        for (ChatbotActionDTO action : visibleActions) {
            holder.actionsContainer.addView(createActionButton(holder.actionsContainer, action, tm));
        }

        holder.actionsWrapper.setVisibility(
                holder.actionsContainer.getChildCount() > 0 ? View.VISIBLE : View.GONE
        );
    }

    @NonNull
    private List<ChatbotActionDTO> visibleActions(@Nullable List<ChatbotActionDTO> actions) {
        List<ChatbotActionDTO> visibleActions = new ArrayList<>();
        if (actions == null || actions.isEmpty()) {
            return visibleActions;
        }
        for (ChatbotActionDTO action : actions) {
            if (shouldShowAction(action)) {
                visibleActions.add(action);
            }
        }
        return visibleActions;
    }

    @NonNull
    private Set<String> actionComparisonKeys(@NonNull List<ChatbotActionDTO> visibleActions) {
        Set<String> keys = new HashSet<>();
        for (ChatbotActionDTO action : visibleActions) {
            String key = comparisonKey(action.getLabel());
            if (!key.isEmpty()) {
                keys.add(key);
            }
        }
        return keys;
    }

    private boolean shouldHideQuickReply(@NonNull String quickReply, @NonNull Set<String> visibleActionKeys) {
        if (visibleActionKeys.isEmpty()) {
            return false;
        }
        String key = comparisonKey(quickReply);
        return !key.isEmpty() && visibleActionKeys.contains(key);
    }

    private TextView createActionButton(@NonNull ViewGroup parent,
                                        @NonNull ChatbotActionDTO action,
                                        @NonNull ThemeManager tm) {
        TextView button = new TextView(parent.getContext());
        button.setText(action.getLabel().trim());
        button.setTextSize(13);
        button.setSingleLine(false);
        button.setMaxLines(2);
        button.setGravity(android.view.Gravity.CENTER);
        button.setPadding(dp(parent, 14), dp(parent, 9), dp(parent, 14), dp(parent, 9));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(parent, 4), 0, dp(parent, 6));
        button.setLayoutParams(params);
        CardThemeHelper.applyPrimaryBubbleSurface(button, button, tm);
        button.setOnClickListener(v -> listener.onActionClick(action));
        return button;
    }

    private boolean shouldShowAction(@Nullable ChatbotActionDTO action) {
        if (action == null || action.getLabel() == null || action.getType() == null) {
            return false;
        }
        String label = action.getLabel().trim();
        String type = action.getType().trim();
        return !label.isEmpty() && isSupportedActionType(type) && listener.shouldShowAction(action);
    }

    private boolean isSupportedActionType(@NonNull String type) {
        switch (type) {
            case "NAV_SUBIR_OBRA":
            case "NAV_SUBIR_SERVICIO":
            case "NAV_MIS_METAS":
            case "NAV_PORTAFOLIO":
            case "NAV_EXPLORAR":
            case "NAV_CARRITO":
            case "NAV_TRANSACCIONES":
            case "NAV_CONVOCATORIAS":
            case "NAV_GESTION_CONVOCATORIAS":
            case "NAV_PERFIL":
            case "NAV_MENSAJES":
            case "NAV_SOLICITUDES":
            case "NAV_NOTIFICACIONES":
            case "NAV_MODERACION":
            case "NAV_GESTION_USUARIOS":
            case "NAV_ESTADISTICAS_PLATAFORMA":
            case "NAV_LOGIN":
            case "NAV_RECUPERAR_CONTRASENA":
                return true;
            default:
                return false;
        }
    }

    @NonNull
    private String comparisonKey(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = normalizeText(value);
        if (normalized.isEmpty()) {
            return "";
        }

        if (normalized.contains("subir obra")) {
            return "subir_obra";
        }
        if (normalized.contains("subir servicio")) {
            return "subir_servicio";
        }
        if (normalized.contains("mis metas") || normalized.contains("crear meta") || normalized.contains("meta")) {
            return "mis_metas";
        }
        if (normalized.contains("portafolio")) {
            return "portafolio";
        }
        if (normalized.contains("explorar")) {
            return "explorar";
        }
        if (normalized.contains("carrito")) {
            return "carrito";
        }
        if (normalized.contains("transaccion")) {
            return "transacciones";
        }
        if (normalized.contains("solicitud")) {
            return "solicitudes";
        }
        if (normalized.contains("notificacion")) {
            return "notificaciones";
        }
        if (normalized.contains("mensaje") || normalized.contains("bandeja")) {
            return "mensajes";
        }
        if (normalized.contains("moderacion") || normalized.contains("reporte")) {
            return "moderacion";
        }
        if (normalized.contains("gestion usuarios") || normalized.contains("usuarios")) {
            return "gestion_usuarios";
        }
        if (normalized.contains("editar convocatorias")) {
            return "gestion_convocatorias";
        }
        if (normalized.contains("estadisticas")) {
            return "estadisticas";
        }
        if (normalized.contains("login") || normalized.contains("iniciar sesion") || normalized.contains("entrar cuenta")) {
            return "login";
        }
        if (normalized.contains("recuperar contrasena") || normalized.contains("restablecer contrasena") || normalized.contains("contrasena")) {
            return "recuperar_contrasena";
        }
        if (normalized.contains("convocatoria")) {
            return "convocatorias";
        }
        if (normalized.contains("perfil")) {
            return "perfil";
        }
        return normalized;
    }

    @NonNull
    private String normalizeText(@NonNull String value) {
        String normalized = Normalizer.normalize(
                value.trim().toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}+", "");

        normalized = normalized
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String previous;
        do {
            previous = normalized;
            normalized = removePrefix(normalized, "ir a mis ");
            normalized = removePrefix(normalized, "ir a mi ");
            normalized = removePrefix(normalized, "ir al ");
            normalized = removePrefix(normalized, "ir a la ");
            normalized = removePrefix(normalized, "ir a los ");
            normalized = removePrefix(normalized, "ir a las ");
            normalized = removePrefix(normalized, "ir a ");
            normalized = removePrefix(normalized, "ver mis ");
            normalized = removePrefix(normalized, "ver mi ");
            normalized = removePrefix(normalized, "ver ");
            normalized = removePrefix(normalized, "mis ");
            normalized = removePrefix(normalized, "mi ");
        } while (!previous.equals(normalized));

        return normalized.replaceAll("\\s+", " ").trim();
    }

    @NonNull
    private String removePrefix(@NonNull String value, @NonNull String prefix) {
        if (value.startsWith(prefix)) {
            return value.substring(prefix.length()).trim();
        }
        return value;
    }

    private TextView createQuickReplyChip(@NonNull ViewGroup parent, @NonNull String text, @NonNull ThemeManager tm) {
        TextView chip = new TextView(parent.getContext());
        chip.setText(text);
        chip.setTextSize(12);
        chip.setSingleLine(false);
        chip.setMaxLines(2);
        chip.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        chip.setPadding(dp(parent, 12), dp(parent, 8), dp(parent, 12), dp(parent, 8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dp(parent, 8));
        params.setMargins(0, 0, dp(parent, 8), dp(parent, 7));
        chip.setLayoutParams(params);
        CardThemeHelper.applySoftChip(chip, tm);
        chip.setOnClickListener(v -> listener.onQuickReplyClick(text));
        return chip;
    }

    private LinearLayout createQuickReplyRow(@NonNull ViewGroup parent) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.START);
        return row;
    }

    private void styleBubbleCard(@NonNull MaterialCardView card, int background, int stroke, int radiusDp, int elevationDp) {
        card.setBackgroundColor(Color.TRANSPARENT);
        card.setCardBackgroundColor(background);
        card.setStrokeColor(stroke);
        card.setStrokeWidth(Math.max(1, dp(card, 1)));
        card.setRadius(dp(card, radiusDp));
        card.setCardElevation(dp(card, elevationDp));
        card.setUseCompatPadding(false);
        card.setPreventCornerOverlap(false);
    }

    private void applyUserBubble(@NonNull UserViewHolder holder, @NonNull UserBubbleStyle bubbleStyle) {
        holder.cardBubble.setBackgroundColor(Color.TRANSPARENT);
        holder.cardBubble.setCardBackgroundColor(Color.TRANSPARENT);
        holder.cardBubble.setStrokeWidth(0);
        holder.cardBubble.setCardElevation(0f);
        holder.cardBubble.setRadius(dp(holder.cardBubble, 22));
        holder.cardBubble.setUseCompatPadding(false);
        holder.cardBubble.setPreventCornerOverlap(false);

        GradientDrawable bubbleDrawable = new GradientDrawable();
        bubbleDrawable.setShape(GradientDrawable.RECTANGLE);
        bubbleDrawable.setColor(ColorUtils.setAlphaComponent(bubbleStyle.backgroundColor, 255));
        bubbleDrawable.setCornerRadius(dp(holder.tvMessage, 22));
        bubbleDrawable.setStroke(Math.max(1, dp(holder.tvMessage, 1)), bubbleStyle.strokeColor);
        holder.tvMessage.setBackground(bubbleDrawable);
    }

    @NonNull
    private UserBubbleStyle resolveUserBubbleStyle(@NonNull ThemeManager tm) {
        int panelSurface = compositeToOpaque(tm.color(ThemeKeys.ACCOUNT_GLASS_PANEL), tm.color(ThemeKeys.BUTTON_SECONDARY_BG));
        int chipSurface = compositeToOpaque(tm.color(ThemeKeys.CARD_CHIP_BG), panelSurface);
        int accentBase = compositeToOpaque(tm.color(ThemeKeys.ACCENT_PRIMARY), chipSurface);
        int accentSoft = compositeToOpaque(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.ACCENT_PRIMARY), 92), chipSurface);
        int accentLight = compositeToOpaque(ColorUtils.setAlphaComponent(tm.color(ThemeKeys.ACCENT_PRIMARY_LIGHT), 116), chipSurface);

        int background = ColorUtils.blendARGB(chipSurface, accentBase, 0.22f);
        background = ColorUtils.blendARGB(background, accentLight, 0.18f);
        background = ColorUtils.blendARGB(background, accentSoft, 0.12f);
        background = ColorUtils.blendARGB(background, panelSurface, 0.20f);

        double luminance = ColorUtils.calculateLuminance(ColorUtils.setAlphaComponent(background, 255));
        if (luminance < 0.24) {
            background = ColorUtils.blendARGB(background, ColorUtils.setAlphaComponent(tm.color(ThemeKeys.BUTTON_TEXT_LIGHT), 255), 0.12f);
        } else if (luminance > 0.84) {
            background = ColorUtils.blendARGB(background, ColorUtils.setAlphaComponent(tm.color(ThemeKeys.TEXT_PRIMARY), 255), 0.05f);
        }

        int stroke = ColorUtils.blendARGB(
                ColorUtils.setAlphaComponent(tm.color(ThemeKeys.CARD_BORDER), 255),
                accentBase,
                0.34f
        );
        stroke = ColorUtils.setAlphaComponent(stroke, 190);

        int text = userBubbleTextColor(
                background,
                tm.color(ThemeKeys.BUTTON_TEXT_DARK),
                tm.color(ThemeKeys.BUTTON_TEXT_LIGHT),
                tm.color(ThemeKeys.CARD_CHIP_TEXT),
                tm.color(ThemeKeys.TEXT_PRIMARY),
                tm.color(ThemeKeys.TEXT_SECONDARY)
        );
        return new UserBubbleStyle(
                ColorUtils.setAlphaComponent(background, 255),
                ColorUtils.setAlphaComponent(stroke, 255),
                ColorUtils.setAlphaComponent(text, 255)
        );
    }

    private int compositeToOpaque(int foreground, int background) {
        return ColorUtils.compositeColors(
                foreground,
                ColorUtils.setAlphaComponent(background, 255)
        );
    }

    private int userBubbleTextColor(int background, int... candidates) {
        int opaqueBackground = ColorUtils.setAlphaComponent(background, 255);
        int best = Color.WHITE;
        double bestContrast = contrast(opaqueBackground, best);
        for (int candidate : candidates) {
            int opaqueCandidate = ColorUtils.setAlphaComponent(candidate, 255);
            double candidateContrast = contrast(opaqueBackground, opaqueCandidate);
            if (candidateContrast > bestContrast) {
                best = opaqueCandidate;
                bestContrast = candidateContrast;
            }
        }
        if (bestContrast >= 4.0) {
            return best;
        }
        return contrast(opaqueBackground, Color.WHITE) >= contrast(opaqueBackground, Color.BLACK)
                ? Color.WHITE
                : Color.BLACK;
    }

    private double contrast(int color1, int color2) {
        return ColorUtils.calculateContrast(color1, color2);
    }

    private int dp(@NonNull View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    private static class UserBubbleStyle {
        private final int backgroundColor;
        private final int strokeColor;
        private final int textColor;

        private UserBubbleStyle(int backgroundColor, int strokeColor, int textColor) {
            this.backgroundColor = backgroundColor;
            this.strokeColor = strokeColor;
            this.textColor = textColor;
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardBubble;
        private final TextView tvMessage;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBubble = itemView.findViewById(R.id.cardChatbotUserBubble);
            tvMessage = itemView.findViewById(R.id.tvChatbotUserMessage);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardBubble;
        private final TextView tvMessage;
        private final View quickRepliesWrapper;
        private final LinearLayout quickRepliesContainer;
        private final View actionsWrapper;
        private final LinearLayout actionsContainer;

        BotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBubble = itemView.findViewById(R.id.cardChatbotBotBubble);
            tvMessage = itemView.findViewById(R.id.tvChatbotBotMessage);
            quickRepliesWrapper = itemView.findViewById(R.id.layoutChatbotQuickRepliesWrapper);
            quickRepliesContainer = itemView.findViewById(R.id.layoutChatbotQuickReplies);
            actionsWrapper = itemView.findViewById(R.id.layoutChatbotActionsWrapper);
            actionsContainer = itemView.findViewById(R.id.layoutChatbotActions);
        }
    }
}
