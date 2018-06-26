package io.openfuture.chain.component

import io.openfuture.chain.domain.key.ExtendedKey

class DerivationKeysHelper {

    companion object {
        private const val PATH_SEPARATOR = "/"
        private const val CORRECT_PATH_PATTERN = "^m([/]?[0-9]+[/]?){1,3}"

        private const val ACCOUNT_NUMBER_POSITION = 1
        private const val WALLET_NUMBER_POSITION = 2
        private const val ADDRESS_NUMBER_POSITION = 3
    }


    fun derive(rootKey: ExtendedKey, derivationPath: String): ExtendedKey {
        if (!CORRECT_PATH_PATTERN.toRegex().matches(derivationPath)) {
            throw Exception("Invalid derivation path")
        }

        val separatedPath = derivationPath.split(PATH_SEPARATOR.toRegex())
        val accountNumber = Integer.parseInt(separatedPath[ACCOUNT_NUMBER_POSITION])

        if (ACCOUNT_NUMBER_POSITION == separatedPath.size - 1) {
            return rootKey.derive(accountNumber)
        }

        val walletNumber = Integer.parseInt(separatedPath[WALLET_NUMBER_POSITION])

        if (WALLET_NUMBER_POSITION == separatedPath.size - 1) {
            return deriveWalletChainKeys(rootKey, accountNumber, walletNumber)
        }

        val addressNumber = Integer.parseInt(separatedPath[ADDRESS_NUMBER_POSITION])
        val base = deriveWalletChainKeys(rootKey, accountNumber, walletNumber)

        return base.derive(addressNumber)
    }

    private fun deriveWalletChainKeys(rootKey: ExtendedKey, accountNumber: Int, walletNumber: Int): ExtendedKey {
        val base = rootKey.derive(accountNumber)
        return base.derive(walletNumber)
    }

}