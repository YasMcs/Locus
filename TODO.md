# TODO: Habilitar Favoritos y Menú Hamburguesa

**Plan Approved**

**Information Gathered:**
- MapaScreen.kt: Custom FAB animation for menu, IconButton for toggleFavorito.
- No ModalNavigationDrawer, custom menuExpandido.
- MapaViewModel.kt: toggleFavorito API calls OK, favoritosIds set.
- LugarEntity.kt: No isFavorite field (uses set check OK).
- NavGraph.kt: 'perfil', 'recuerdos' OK, no 'historial' or 'favoritos'.
- ApiService.kt: Favoritos endpoints OK.

**Issues:**
- No Navigation Drawer – custom FAB menu.
- Favoritos responds but no local isFavorite.
- No 'historial' route.

**Plan:**
1. MapaScreen.kt: Add ModalNavigationDrawer, drawerState, drawerContent with nav items.
2. Change FAB Menu onClick to drawerState.open().
3. Add isFavorite to LugarEntity (transient).
4. Add 'favoritos' and 'historial' routes/screens in NavGraph (stub HistorialScreen).
5. Update toggleFavorito to update local isFavorite.

1. LugarEntity isFavorite added ✅

**Next: Update MapaViewModel to set isFavorite when loading lugares.**

