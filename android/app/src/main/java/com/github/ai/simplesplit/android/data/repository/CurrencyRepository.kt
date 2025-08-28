package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.data.api.coverters.toCurrencies
import com.github.ai.simplesplit.android.data.database.dao.CurrencyEntityDao
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.model.exception.AppException

class CurrencyRepository(
    private val api: ApiClient,
    private val dao: CurrencyEntityDao
) {

    fun getAllCached(): Either<AppException, List<CurrencyEntity>> =
        either {
            val currencies = dao.getAll()
            if (currencies.isEmpty()) {
                raise(AppException(message = "Unable to load currencies"))
            }

            currencies
        }

    suspend fun getAllOrDownload(): Either<AppException, List<CurrencyEntity>> =
        either {
            val localCurrencies = dao.getAll()
            if (localCurrencies.isNotEmpty()) {
                localCurrencies
            } else {
                downloadAndSave().bind()
            }
        }

    private suspend fun downloadAndSave(): Either<AppException, List<CurrencyEntity>> =
        either {
            val getCurrenciesResult = api.getCurrencies().bind()

            val remoteCurrencies = getCurrenciesResult.currencies.toCurrencies()

            mergeEntities(
                localEntities = dao.getAll(),
                remoteEntities = remoteCurrencies,
                entityToUidMapper = { currency -> currency.isoCode },
                isEqual = { local, remote -> local == remote },
                onInsert = { remote -> dao.insert(remote) },
                onUpdate = { _, remote -> dao.update(remote) },
                onDelete = { local -> dao.deleteByIsoCode(local.isoCode) }
            )

            dao.getAll()
        }
}