package tk.martijn_heil.wac_core.command.specific.provider;


public class EPlayerProvider //implements Provider<EPlayer>
{
//    private final Server server;
//    private Provider<Player> playerProvider;
//
//
//    public EPlayerProvider(Server server, Provider<Player> playerProvider)
//    {
//        this.server = server;
//        this.playerProvider = playerProvider;
//    }
//
//
//    @Override
//    public boolean isProvided()
//    {
//        return false;
//    }
//
//
//    @Nullable
//    @Override
//    public EPlayer get(CommandArgs commandArgs, List<? extends Annotation> list) throws ArgumentException, ProvisionException
//    {
//        Player player = playerProvider.get(commandArgs, list);
//        EPlayer ep = ElytraOptions.getEPlayer(player);
//        if(ep == null) throw new ArgumentParseException("Player not found.");
//        return ep;
//    }
//
//
//    @Override
//    public List<String> getSuggestions(String s)
//    {
//        List<String> eMatches = new ArrayList<>();
//        for (Player p : server.matchPlayer(s))
//        {
//            EPlayer ep = ElytraOptions.getEPlayer(p);
//            if(ep != null)
//            {
//                eMatches.add(ep.getPlayer().getName());
//            }
//        }
//
//        return eMatches;
//    }
}
