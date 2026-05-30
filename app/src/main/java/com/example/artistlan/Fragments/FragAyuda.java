package com.example.artistlan.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artistlan.Activitys.ActFragmentoPrincipal;
import com.example.artistlan.Activitys.ActIniciarSesion;
import com.example.artistlan.Activitys.ActRecuperarContrasena;
import com.example.artistlan.Conector.RetrofitClient;
import com.example.artistlan.Conector.SessionManager;
import com.example.artistlan.Conector.api.ChatbotApi;
import com.example.artistlan.Conector.model.ChatbotActionDTO;
import com.example.artistlan.Conector.model.ChatbotRequestDTO;
import com.example.artistlan.Conector.model.ChatbotResponseDTO;
import com.example.artistlan.R;
import com.example.artistlan.Theme.ThemeApplier;
import com.example.artistlan.Theme.ThemeManager;
import com.example.artistlan.Theme.ThemeModuleStyler;
import com.example.artistlan.adapter.ChatbotMessageAdapter;
import com.example.artistlan.model.ChatbotMessageUi;
import com.example.artistlan.utils.CardThemeHelper;

import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragAyuda extends Fragment {

    private static final String CHATBOT_PREFS = "artistlan_chatbot_prefs";
    private static final String KEY_CHATBOT_SESSION_ID = "artistlan_chatbot_session_id";
    private static final String INITIAL_MESSAGE = "Hola, soy el asistente de Artistlan. Puedo ayudarte con publicaciones, búsqueda, carrito, solicitudes, transacciones, reportes y seguridad de cuenta.";
    private static final long ACTION_NAVIGATION_DEBOUNCE_MS = 650L;
    private static final List<String> INITIAL_QUICK_REPLIES = List.of(
            "Subir obra",
            "Explorar contenido",
            "Carrito y compras",
            "Seguridad de cuenta"
    );

    private View rootView;
    private RecyclerView recyclerMessages;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private View header;
    private View composer;
    private View composerSurface;
    private View topMenuFrame;
    private View bottomMenuFrame;
    private ViewTreeObserver.OnPreDrawListener menuInsetListener;
    private int lastHeaderTopPadding = -1;
    private int lastComposerBottomPadding = -1;
    private ChatbotMessageAdapter adapter;
    private ChatbotApi chatbotApi;
    private String sessionId;
    private Call<ChatbotResponseDTO> currentCall;
    private ChatbotMessageUi loadingMessage;
    private boolean requestInFlight;
    private boolean resumeThemeApplied;
    private long lastActionNavigationAt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_frag_ayuda, container, false);
        ThemeModuleStyler.styleFragment(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatbotApi = RetrofitClient.getClient().create(ChatbotApi.class);
        sessionId = getOrCreateSessionId();
        initViews(view);
        setupChat();
        applyChatTheme();
        setupMenuAwareInsets();
        addInitialMessage();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rootView != null && resumeThemeApplied) {
            ThemeModuleStyler.styleFragment(this, rootView);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            applyChatTheme();
        }
        resumeThemeApplied = true;
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) {
            currentCall.cancel();
        }
        removeMenuAwareInsets();
        currentCall = null;
        loadingMessage = null;
        requestInFlight = false;
        recyclerMessages = null;
        etMessage = null;
        btnSend = null;
        tvTitle = null;
        tvSubtitle = null;
        header = null;
        composer = null;
        composerSurface = null;
        topMenuFrame = null;
        bottomMenuFrame = null;
        menuInsetListener = null;
        lastHeaderTopPadding = -1;
        lastComposerBottomPadding = -1;
        adapter = null;
        rootView = null;
        super.onDestroyView();
    }

    private void initViews(@NonNull View view) {
        header = view.findViewById(R.id.layoutChatbotHeader);
        composer = view.findViewById(R.id.layoutChatbotComposer);
        recyclerMessages = view.findViewById(R.id.recyclerChatbotMessages);
        etMessage = view.findViewById(R.id.etChatbotMessage);
        btnSend = view.findViewById(R.id.btnChatbotSend);
        tvTitle = view.findViewById(R.id.tvChatbotTitle);
        tvSubtitle = view.findViewById(R.id.tvChatbotSubtitle);
        composerSurface = view.findViewById(R.id.layoutChatbotComposerSurface);
    }

    private void setupChat() {
        adapter = new ChatbotMessageAdapter(new ChatbotMessageAdapter.ChatbotInteractionListener() {
            @Override
            public void onQuickReplyClick(@NonNull String text) {
                sendUserMessage(text);
            }

            @Override
            public void onActionClick(@NonNull ChatbotActionDTO action) {
                handleChatbotAction(action);
            }

            @Override
            public boolean shouldShowAction(@NonNull ChatbotActionDTO action) {
                return isActionAllowedForCurrentUser(action.getType());
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(false);
        recyclerMessages.setLayoutManager(layoutManager);
        recyclerMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendFromInput());
        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendFromInput();
                return true;
            }
            return false;
        });
    }

    private void handleChatbotAction(@Nullable ChatbotActionDTO action) {
        if (action == null || action.getType() == null) {
            return;
        }

        String type = action.getType().trim();
        int destinationId = destinationForAction(type);
        boolean centroMensajesAction = isCentroMensajesAction(type);
        boolean authAction = isAuthAction(type);
        if (destinationId == View.NO_ID && !centroMensajesAction && !authAction) {
            return;
        }

        if (!authAction && !isActionAllowedForCurrentUser(type)) {
            addBotError("Esta opción solo está disponible para cuentas con permisos especiales.");
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (now - lastActionNavigationAt < ACTION_NAVIGATION_DEBOUNCE_MS) {
            return;
        }
        lastActionNavigationAt = now;

        try {
            if (authAction) {
                openAuthAction(type);
                return;
            }

            if (centroMensajesAction) {
                openCentroMensajesAction(type);
                return;
            }

            NavController navController = NavHostFragment.findNavController(this);
            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId() == destinationId) {
                return;
            }
            navController.navigate(destinationId);
        } catch (ActivityNotFoundException | IllegalArgumentException | IllegalStateException ex) {
            addBotError(authAction
                    ? "No pude abrir esa pantalla en este momento."
                    : "No pude abrir esa sección en este momento.");
        }
    }

    private boolean isCentroMensajesAction(@NonNull String type) {
        switch (type) {
            case "NAV_MENSAJES":
            case "NAV_SOLICITUDES":
            case "NAV_NOTIFICACIONES":
                return true;
            default:
                return false;
        }
    }

    private boolean isAuthAction(@NonNull String type) {
        switch (type) {
            case "NAV_LOGIN":
            case "NAV_RECUPERAR_CONTRASENA":
                return true;
            default:
                return false;
        }
    }

    private void openAuthAction(@NonNull String type) {
        if ("NAV_LOGIN".equals(type)) {
            if (isUserLoggedIn()) {
                addBotError("Ya tienes una sesión activa en Artistlan.");
                return;
            }
            startActivity(new Intent(requireContext(), ActIniciarSesion.class));
            return;
        }

        if (isUserLoggedIn()) {
            addBotError("Puedes gestionar la seguridad de tu cuenta desde tu perfil.");
            return;
        }
        startActivity(new Intent(requireContext(), ActRecuperarContrasena.class));
    }

    private void openCentroMensajesAction(@NonNull String type) {
        if (!(getActivity() instanceof ActFragmentoPrincipal)) {
            throw new IllegalStateException("Host activity no disponible");
        }

        ActFragmentoPrincipal activity = (ActFragmentoPrincipal) getActivity();
        if ("NAV_SOLICITUDES".equals(type)) {
            activity.abrirCentroMensajes(1, FragSolicitudesMensajes.MODO_RECIBIDAS);
            return;
        }

        activity.abrirCentroMensajes(0);
    }

    private boolean isActionAllowedForCurrentUser(@Nullable String type) {
        if (type == null) {
            return false;
        }
        String cleanType = type.trim();
        if ("NAV_MODERACION".equals(cleanType)) {
            return isAdmin() || isModerator();
        }
        if ("NAV_GESTION_USUARIOS".equals(cleanType)) {
            return isAdmin();
        }
        if ("NAV_LOGIN".equals(cleanType)) {
            return !isUserLoggedIn();
        }
        return true;
    }

    private boolean isUserLoggedIn() {
        Context context = getContext();
        return context != null && new SessionManager(context).isLoggedIn();
    }

    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getCurrentUserRole());
    }

    private boolean isModerator() {
        return "MODERADOR".equalsIgnoreCase(getCurrentUserRole());
    }

    @NonNull
    private String getCurrentUserRole() {
        Context context = getContext();
        if (context == null) {
            return "USER";
        }
        SharedPreferences prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        String role = prefs.getString("rol", "USER");
        return role != null && !role.trim().isEmpty() ? role.trim() : "USER";
    }

    private int destinationForAction(@NonNull String type) {
        switch (type) {
            case "NAV_SUBIR_OBRA":
                return R.id.fragSubirObra;
            case "NAV_SUBIR_SERVICIO":
                return R.id.fragSubirServicio;
            case "NAV_PORTAFOLIO":
                return R.id.fragPortafolio;
            case "NAV_EXPLORAR":
                return R.id.fragExplorar;
            case "NAV_CARRITO":
                return R.id.fragCarrito;
            case "NAV_TRANSACCIONES":
                return R.id.fragTransacciones;
            case "NAV_CONVOCATORIAS":
                return R.id.navCalendario;
            case "NAV_PERFIL":
                return R.id.fragVerPerfil;
            case "NAV_MODERACION":
                return R.id.fragModeracionReportes;
            case "NAV_GESTION_USUARIOS":
                return R.id.fragAdminGestionUsuarios;
            default:
                return View.NO_ID;
        }
    }

    private void applyChatTheme() {
        if (!isAdded()) {
            return;
        }
        ThemeManager tm = new ThemeManager(requireContext());
        ThemeApplier.applyTextPrimary(tvTitle, tm);
        ThemeApplier.applyTextSecondary(tvSubtitle, tm);
        ThemeApplier.applyInput(etMessage, tm);
        CardThemeHelper.applyThemedSurface(composerSurface, tm, 24);
        CardThemeHelper.applyPrimaryBubbleButton(btnSend, tm);
    }

    private void setupMenuAwareInsets() {
        if (rootView == null || getActivity() == null) {
            return;
        }
        topMenuFrame = getActivity().findViewById(R.id.topBarFrame);
        bottomMenuFrame = getActivity().findViewById(R.id.MenuInferiorFrame);
        updateMenuAwareInsets();

        menuInsetListener = () -> {
            updateMenuAwareInsets();
            return true;
        };
        rootView.getViewTreeObserver().addOnPreDrawListener(menuInsetListener);
    }

    private void removeMenuAwareInsets() {
        if (rootView == null || menuInsetListener == null) {
            return;
        }
        ViewTreeObserver observer = rootView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.removeOnPreDrawListener(menuInsetListener);
        }
    }

    private void updateMenuAwareInsets() {
        if (header == null || composer == null) {
            return;
        }

        int headerTop = visibleTopMenuHeight() + dp(8);
        int composerBottom = visibleBottomMenuHeight() + dp(10);

        if (headerTop != lastHeaderTopPadding) {
            header.setPadding(
                    header.getPaddingLeft(),
                    headerTop,
                    header.getPaddingRight(),
                    header.getPaddingBottom()
            );
            lastHeaderTopPadding = headerTop;
        }

        if (composerBottom != lastComposerBottomPadding) {
            composer.setPadding(
                    composer.getPaddingLeft(),
                    composer.getPaddingTop(),
                    composer.getPaddingRight(),
                    composerBottom
            );
            lastComposerBottomPadding = composerBottom;
        }
    }

    private int visibleTopMenuHeight() {
        if (topMenuFrame == null || topMenuFrame.getVisibility() != View.VISIBLE || topMenuFrame.getHeight() <= 0) {
            return 0;
        }
        float visible = topMenuFrame.getHeight() + topMenuFrame.getTranslationY();
        return Math.max(0, Math.round(visible));
    }

    private int visibleBottomMenuHeight() {
        if (bottomMenuFrame == null || bottomMenuFrame.getVisibility() != View.VISIBLE || bottomMenuFrame.getHeight() <= 0) {
            return 0;
        }
        float visible = bottomMenuFrame.getHeight() - bottomMenuFrame.getTranslationY();
        return Math.max(0, Math.round(visible));
    }

    private void addInitialMessage() {
        adapter.addMessage(ChatbotMessageUi.bot(
                INITIAL_MESSAGE,
                "DEFAULT_WELCOME",
                "LOCAL_UI",
                INITIAL_QUICK_REPLIES,
                null
        ));
        scrollToBottom();
    }

    private void sendFromInput() {
        if (etMessage == null) {
            return;
        }
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (text.isEmpty()) {
            return;
        }
        etMessage.setText("");
        sendUserMessage(text);
    }

    private void sendUserMessage(@NonNull String text) {
        String cleanText = text.trim();
        if (cleanText.isEmpty() || requestInFlight || adapter == null) {
            return;
        }

        adapter.addMessage(ChatbotMessageUi.user(cleanText));
        showLoadingMessage();
        scrollToBottom();
        setComposerEnabled(false);

        ChatbotRequestDTO request = new ChatbotRequestDTO(cleanText, sessionId, getCurrentUserId());
        currentCall = chatbotApi.enviarMensaje(request);
        currentCall.enqueue(new Callback<ChatbotResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChatbotResponseDTO> call,
                                   @NonNull Response<ChatbotResponseDTO> response) {
                if (!isActiveCall(call)) {
                    return;
                }
                removeLoadingMessage();
                setComposerEnabled(true);

                ChatbotResponseDTO body = response.body();
                if (!response.isSuccessful() || body == null) {
                    addBotError("No recibí una respuesta válida. Intenta de nuevo.");
                    return;
                }

                String reply = body.getReply() != null ? body.getReply().trim() : "";
                if (reply.isEmpty()) {
                    addBotError("No recibí una respuesta válida. Intenta de nuevo.");
                    return;
                }

                adapter.addMessage(ChatbotMessageUi.bot(
                        reply,
                        body.getIntent(),
                        body.getSource(),
                        body.getQuickReplies(),
                        body.getActions()
                ));
                scrollToBottom();
            }

            @Override
            public void onFailure(@NonNull Call<ChatbotResponseDTO> call, @NonNull Throwable t) {
                if (!isActiveCall(call) || call.isCanceled()) {
                    return;
                }
                removeLoadingMessage();
                setComposerEnabled(true);
                addBotError("No pude conectar con el asistente en este momento. Intenta de nuevo.");
            }
        });
    }

    private void showLoadingMessage() {
        requestInFlight = true;
        loadingMessage = ChatbotMessageUi.loading("Artistlan está escribiendo...");
        adapter.addMessage(loadingMessage);
    }

    private void removeLoadingMessage() {
        requestInFlight = false;
        if (loadingMessage != null && adapter != null) {
            adapter.removeMessage(loadingMessage);
        }
        loadingMessage = null;
        currentCall = null;
    }

    private void addBotError(@NonNull String message) {
        if (adapter == null) {
            return;
        }
        adapter.addMessage(ChatbotMessageUi.bot(message, "LOCAL_ERROR", "LOCAL_UI", null, null));
        scrollToBottom();
    }

    private boolean isActiveCall(@NonNull Call<ChatbotResponseDTO> call) {
        return isAdded() && adapter != null && currentCall == call;
    }

    private void setComposerEnabled(boolean enabled) {
        if (etMessage != null) {
            etMessage.setEnabled(enabled);
        }
        if (btnSend != null) {
            btnSend.setEnabled(enabled);
        }
    }

    private void scrollToBottom() {
        if (recyclerMessages == null || adapter == null || adapter.getItemCount() == 0) {
            return;
        }
        updateMenuAwareInsets();
        recyclerMessages.post(() -> {
            if (recyclerMessages != null && adapter != null && adapter.getItemCount() > 0) {
                recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
                recyclerMessages.postDelayed(() -> {
                    if (recyclerMessages != null && adapter != null && adapter.getItemCount() > 0) {
                        recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 80L);
            }
        });
    }

    private int dp(int value) {
        Context context = getContext();
        if (context == null) {
            return value;
        }
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    private String getOrCreateSessionId() {
        SharedPreferences prefs = requireContext().getSharedPreferences(CHATBOT_PREFS, Context.MODE_PRIVATE);
        String stored = prefs.getString(KEY_CHATBOT_SESSION_ID, null);
        if (stored != null && !stored.trim().isEmpty()) {
            return stored.trim();
        }

        String generated = UUID.randomUUID().toString();
        prefs.edit().putString(KEY_CHATBOT_SESSION_ID, generated).apply();
        return generated;
    }

    @Nullable
    private Integer getCurrentUserId() {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        SharedPreferences prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        int idUsuario = prefs.getInt("idUsuario", prefs.getInt("id", -1));
        return idUsuario > 0 ? idUsuario : null;
    }
}
