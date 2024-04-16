package no.nav.helseidnavtest.dialogmelding

import no.nav.helseopplysninger.hodemelding.ObjectFactory

object MsgHeaderUtil {

    val HMOF =  ObjectFactory()

    fun avsender() =
        HMOF.createXMLSender().apply {
            organisation = HMOF.createXMLOrganisation().apply {
                organisationName = "NAV"
                ident.add(HMOF.createXMLIdent().apply {
                    id = "889640782"
                    typeId = HMOF.createXMLCV().apply {
                        dn = "Organisasjonsnummeret i Enhetsregisteret"
                        s = "2.16.578.1.12.4.1.1.9051"
                        v = "ENH"
                    }
                })
                ident.add(HMOF.createXMLIdent().apply {
                    id = "79768"
                    typeId = HMOF.createXMLCV().apply {
                        dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                        s = "2.16.578.1.12.4.1.1.9051"
                        v = "HER"
                    }
                })
            }
        }

    fun pasient(arbeidstaker: Arbeidstaker) =
        HMOF.createXMLPatient().apply {
            with(arbeidstaker) {
                familyName = etternavn
                middleName = mellomnavn
                givenName = fornavn
                ident.add(createXMLIdentForPersonident(arbeidstakerPersonident))
            }
        }

    fun mottaker(bestiling: Dialogmelding) =
        HMOF.createXMLReceiver().apply {
            with(bestiling.behandler)  {
                organisation = HMOF.createXMLOrganisation().apply {
                    organisationName = kontor.navn
                    ident.add(HMOF.createXMLIdent().apply {
                        id = kontor.herId.toString()
                        typeId = HMOF.createXMLCV().apply {
                            dn = "Identifikator fra Helsetjenesteenhetsregisteret (HER-id)"
                            s = "2.16.578.1.12.4.1.1.9051"
                            v = "HER"
                        }
                    })
                    kontor.orgnummer?.let { orgnummer ->
                        ident.add(HMOF.createXMLIdent().apply {
                            id = orgnummer.value
                            typeId = HMOF.createXMLCV().apply {
                                dn = "Organisasjonsnummeret i Enhetsregisteret"
                                s = "2.16.578.1.12.4.1.1.9051"
                                v = "ENH"
                            }
                        })
                    }
                    address = HMOF.createXMLAddress().apply {
                        type = HMOF.createXMLCS().apply {
                            dn = "Besøksadresse"
                            v = "RES"
                        }
                        streetAdr = kontor.adresse
                        postalCode = kontor.postnummer
                        city = kontor.poststed
                    }
                    healthcareProfessional = HMOF.createXMLHealthcareProfessional().apply {
                        familyName = etternavn
                        middleName = mellomnavn
                        givenName = fornavn
                        personident?.let {
                            ident.add(createXMLIdentForPersonident(it))
                        }
                        hprId?.let {
                            ident.add(HMOF.createXMLIdent().apply {
                                id = "$it"
                                typeId = HMOF.createXMLCV().apply {
                                    dn = "HPR-nummer"
                                    s = "2.16.578.1.12.4.1.1.8116"
                                    v = "HPR"
                                }
                            })
                        }
                        herId?.let {
                            ident.add(HMOF.createXMLIdent().apply {
                                id = "$it"
                                typeId = HMOF.createXMLCV().apply {
                                    dn = "Identifikator fra Helsetjenesteenhetsregisteret"
                                    s = "2.16.578.1.12.4.1.1.8116"
                                    v = "HER"
                                }
                            })
                        }
                    }
                }
            }
        }

}