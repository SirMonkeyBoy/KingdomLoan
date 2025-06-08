# Loan Plugin for Minecraft

Commands:
- `/loan`: Displays a list of subcommands.
  - `/loan help`: Displays a list of subcommands.
  - `/loan create (User you're loaning to) (Amount you're loaning) (Amount they need to pay back)`:
     Sends a message to the player you are loaning to with the amount they are getting loaned and the amount they need to pay back.
  - `/loan accept (User loaning you money)`:
     Accepts the loan if you have any pending request for that player loan requests have a 120 seconds time out.
  - `/loan pay (Amount)`: Pays down your loan.
  - `/loan list`: Shows you the active loan have or a list of all active loans you have given out.
  - `/loan history (had/given) (page)`: List of all loans have had or list of all loans you have given out.

You can give out as many loans as you have money for but the player you have loaned the money to can only have one loan at a time
and a player who has been loaned money can't loan out any money until there loan is paid off.

Needs a MariaDB Database to work plugin won't start without it.

This is all subject to change.
