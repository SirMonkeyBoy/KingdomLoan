# Loan Plugin for Minecraft

Commands:
- `/loan`: Displays a list of subcommands.
  - `/loan help`: Displays a list of subcommands.
  - `/loan create (User you're lending to) (Amount you're lending) (Amount they need to pay back)`:
     Sends a message to the player you are lending to with the amount they are getting lent and the amount they need to pay back.
  - `/loan accept (User lending you money)`:
     Accepts the loan if you have any pending request for that player, loan requests have a 120 seconds time out.
  - `/loan pay (Amount)`: Pays down your loan.
  - `/loan list`: Active loans you have or have given out.
  - `/loan history (borrowed/lent) (page)`: List of all loans have had or list of all loans you have given out.

You can give out as many loans as you have money for but the player you have lent the money to can only have one loan at a time
and a player who has been lent money can't loan out any money until their loan is paid off.

Command Usage examples:
  - `/loan help` Shows subcommands.
  - `/loan create SirMonkeyBoy 100000 150000` Loan request sent to SirMonkeyBoy also sends the borrower a message with loan info.
  - `/loan accept Monkey` Accepted loan from Monkey for \$100000 and pay back amount is \$150000 also sends a message to the lender.
  - `/loan pay` If you have an active loan it will tell you how much is left to pay.
  - `/loan pay 10000` Pays down your loan by 10000.
  - `/loan list` Active loans you have or have given out.
  - `/loan history borrowed 1` List of loans you have borrowed up to 10 loans per page.
  - `/loan history lent 1` List of loans you have lent up to 10 loans per page.

Features:
- MariaDB for data storage.
- Keeps a record of all Loans in the table LoanHistory.
- Cooldown timer for all commands that use the database configurable in config.yml.
- Configurable loan minimum in config.yml.

TODO
- Add /loan history.

Needs a MariaDB Database to work plugin won't start without it.

This is all subject to change.
